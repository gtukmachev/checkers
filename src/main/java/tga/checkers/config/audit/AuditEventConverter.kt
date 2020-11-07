package tga.checkers.config.audit

import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import tga.checkers.domain.PersistentAuditEvent
import java.util.*
import kotlin.collections.HashMap

@Component
class AuditEventConverter {
    /**
     * Convert a list of [PersistentAuditEvent]s to a list of [AuditEvent]s.
     *
     * @param persistentAuditEvents the list to convert.
     * @return the converted list.
     */
    fun convertToAuditEvent(persistentAuditEvents: Iterable<PersistentAuditEvent>): List<AuditEvent> {
        return persistentAuditEvents.map(::convertToAuditEvent).toList()
    }

    /**
     * Convert a [PersistentAuditEvent] to an [AuditEvent].
     *
     * @param persistentAuditEvent the event to convert.
     * @return the converted list.
     */
    fun convertToAuditEvent(persistentAuditEvent: PersistentAuditEvent): AuditEvent {
        return AuditEvent(
            persistentAuditEvent.auditEventDate,
            persistentAuditEvent.principal,
            persistentAuditEvent.auditEventType,
            convertDataToObjects(persistentAuditEvent.data)
        )
    }

    /**
     * Internal conversion. This is needed to support the current SpringBoot actuator `AuditEventRepository` interface.
     *
     * @param data the data to convert.
     * @return a map of [String], [Object].
     */
    fun convertDataToObjects(data: Map<String, String>): Map<String, Any> {
        val results: MutableMap<String, Any> = HashMap()
        for ((key, value) in data) { results[key] = value }
        return results
    }

    /**
     * Internal conversion. This method will allow to save additional data.
     * By default, it will save the object as string.
     *
     * @param data the data to convert.
     * @return a map of [String], [String].
     */
    fun convertDataToStrings(data: Map<String, Any>): Map<String, String> {
        val results: MutableMap<String, String> = HashMap()
        for ((key, value) in data) {
            // Extract the data that will be saved.
            if (value is WebAuthenticationDetails) {
                results["remoteAddress"] = value.remoteAddress
                results["sessionId"] = value.sessionId
            } else {
                results[key] = Objects.toString(value)
            }
        }
        return results
    }
}
