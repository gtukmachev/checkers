package tga.checkers.service.dto

import tga.checkers.config.Constants
import tga.checkers.domain.Authority
import tga.checkers.domain.User
import java.time.Instant
import java.util.stream.Collectors
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A DTO representing a user, with his authorities.
 */
open class UserDTO(
    var id: Long = 0,
    var login: @NotBlank @Pattern(regexp = Constants.LOGIN_REGEX) @Size(min = 1, max = 50) String = "",
    var firstName: @Size(max = 50) String = "",
    var lastName: @Size(max = 50) String = "",
    var email: @Email @Size(min = 5, max = 254) String = "",
    var isActivated: Boolean = false,
    var imageUrl: @Size(max = 256) String? = null,
    var langKey: @Size(min = 2, max = 10) String? = null,
    var createdBy: String? = null,
    var createdDate: Instant? = null,
    var lastModifiedBy: String? = null,
    var lastModifiedDate: Instant? = null,
    var authorities: Set<String> = setOf()
) {

    constructor(user: User): this(
        id = user.id,
        login = user.login,
        firstName = user.firstName,
        lastName = user.lastName,
        email = user.email,
        isActivated = user.activated,
        imageUrl = user.imageUrl,
        langKey = user.langKey,
        createdBy = user.createdBy,
        createdDate = user.createdDate,
        lastModifiedBy = user.lastModifiedBy,
        lastModifiedDate = user.lastModifiedDate,
        authorities = user.authorities.asSequence().map{ it.name }.toSet()
    )

    override fun toString(): String {
        return "UserDTO(id=$id, login='$login', firstName='$firstName', lastName='$lastName', email='$email', isActivated=$isActivated, imageUrl=$imageUrl, langKey=$langKey, createdBy=$createdBy, createdDate=$createdDate, lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, authorities=$authorities)"
    }


}
