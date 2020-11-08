package tga.checkers.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

//todo: replace the entity with a List<a enum> or String in User entity
/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Table(name = "jhi_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Authority : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    @Id
    @Column(length = 50)
    var name: @NotNull @Size(max = 50) String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other !is Authority) {
            false
        } else name == other.name
    }

    override fun hashCode(): Int {
        return Objects.hashCode(name)
    }

    override fun toString(): String = "Authority(name=$name)"


}
