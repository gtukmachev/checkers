package tga.checkers.security

import org.springframework.security.core.AuthenticationException

/**
 * This exception is thrown in case of a not activated user trying to authenticate.
 */
class UserNotActivatedException(message: String) : AuthenticationException(message)
