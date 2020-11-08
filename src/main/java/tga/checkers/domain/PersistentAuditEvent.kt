package tga.checkers.domain

import java.io.Serializable
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Entity
@Table(name = "jhi_persistent_audit_event")
class PersistentAuditEvent : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "event_id")
    var id: Long = 0

    @Column(nullable = false)
    var principal: @NotNull String = ""

    @Column(name = "event_date")
    var auditEventDate: Instant? = null

    @Column(name = "event_type")
    var auditEventType: String? = null

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "jhi_persistent_audit_evt_data", joinColumns = [JoinColumn(name = "event_id")])
    var data: Map<String, String> = HashMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other !is PersistentAuditEvent) {
            false
        } else id != 0L && id == other.id
    }

    override fun hashCode(): Int {
        return 31
    }

    override fun toString(): String {
        return "PersistentAuditEvent(id=$id, principal='$principal', auditEventDate=$auditEventDate, auditEventType=$auditEventType, data=$data)"
    }

}
