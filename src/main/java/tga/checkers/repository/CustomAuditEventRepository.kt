package tga.checkers.repository

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tga.checkers.config.Constants
import tga.checkers.config.audit.AuditEventConverter
import tga.checkers.domain.PersistentAuditEvent
import java.time.Instant

/**
 * An implementation of Spring Boot's [AuditEventRepository].
 */
@Repository
open class CustomAuditEventRepository(private val persistenceAuditEventRepository: PersistenceAuditEventRepository,
                                 private val auditEventConverter: AuditEventConverter) : AuditEventRepository {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun find(principal: String, after: Instant, type: String): List<AuditEvent> {
        val persistentAuditEvents: Iterable<PersistentAuditEvent> = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after, type)
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun add(event: AuditEvent) {
        if (AUTHORIZATION_FAILURE != event.type &&
            Constants.ANONYMOUS_USER != event.principal) {
            val persistentAuditEvent = PersistentAuditEvent()
            persistentAuditEvent.principal = event.principal
            persistentAuditEvent.auditEventType = event.type
            persistentAuditEvent.auditEventDate = event.timestamp
            val eventData = auditEventConverter.convertDataToStrings(event.data)
            persistentAuditEvent.data = truncate(eventData)
            persistenceAuditEventRepository.save(persistentAuditEvent)
        }
    }

    /**
     * Truncate event data that might exceed column length.
     */
    private fun truncate(data: Map<String, String>?): Map<String, String?> {
        val results: MutableMap<String, String?> = HashMap()
        if (data != null) {
            for (entry in data.entries) {
                var value = entry.value
                if (value != null) {
                    val length = value.length
                    if (length > EVENT_DATA_COLUMN_MAX_LENGTH) {
                        value = value.substring(0, EVENT_DATA_COLUMN_MAX_LENGTH)
                        log.warn("Event data for {} too long ({}) has been truncated to {}. Consider increasing column width.",
                            entry.key, length, EVENT_DATA_COLUMN_MAX_LENGTH)
                    }
                }
                results[entry.key] = value
            }
        }
        return results
    }

    companion object {
        private const val AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE"

        /**
         * Should be the same as in Liquibase migration.
         */
        protected const val EVENT_DATA_COLUMN_MAX_LENGTH = 255
    }
}