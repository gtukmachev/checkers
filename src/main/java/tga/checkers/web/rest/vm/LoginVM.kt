package tga.checkers.web.rest.vm

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * View Model object for storing a user's credentials.
 */
data class LoginVM (
    var username: @NotNull @Size(min = 1, max = 50) String,
    var password: @NotNull @Size(min = 4, max = 100) String,
    var isRememberMe: Boolean = false
)
