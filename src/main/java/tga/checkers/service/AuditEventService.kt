package tga.checkers.service

import io.github.jhipster.config.JHipsterProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tga.checkers.config.audit.AuditEventConverter
import tga.checkers.domain.PersistentAuditEvent
import tga.checkers.repository.PersistenceAuditEventRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.Consumer

/**
 * Service for managing audit events.
 *
 *
 * This is the default implementation to support SpringBoot Actuator `AuditEventRepository`.
 */
@Service
@Transactional
open class AuditEventService(
    private val persistenceAuditEventRepository: PersistenceAuditEventRepository,
    private val auditEventConverter: AuditEventConverter, private val jHipsterProperties: JHipsterProperties) {
    private val log = LoggerFactory.getLogger(AuditEventService::class.java)

    /**
     * Old audit events should be automatically deleted after 30 days.
     *
     * This is scheduled to get fired at 12:00 (am).
     */
    @Scheduled(cron = "0 0 12 * * ?")
    fun removeOldAuditEvents() {
        persistenceAuditEventRepository
            .findByAuditEventDateBefore(Instant.now().minus(jHipsterProperties.auditEvents.retentionPeriod.toLong(), ChronoUnit.DAYS))
            .forEach(Consumer { auditEvent: PersistentAuditEvent ->
                log.debug("Deleting audit data {}", auditEvent)
                persistenceAuditEventRepository.delete(auditEvent)
            })
    }

    @Transactional(readOnly = true)
    open fun findAll(pageable: Pageable?): Page<AuditEvent> {
        return persistenceAuditEventRepository.findAll(pageable)
            .map { persistentAuditEvent: PersistentAuditEvent? -> auditEventConverter.convertToAuditEvent(persistentAuditEvent!!) }
    }

    @Transactional(readOnly = true)
    open fun findByDates(fromDate: Instant?, toDate: Instant?, pageable: Pageable?): Page<AuditEvent> {
        return persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate!!, toDate!!, pageable!!)
            .map { persistentAuditEvent: PersistentAuditEvent? -> auditEventConverter.convertToAuditEvent(persistentAuditEvent!!) }
    }

    @Transactional(readOnly = true)
    open fun find(id: Long): Optional<AuditEvent> {
        return persistenceAuditEventRepository.findById(id)
            .map { persistentAuditEvent: PersistentAuditEvent? -> auditEventConverter.convertToAuditEvent(persistentAuditEvent!!) }
    }
}
