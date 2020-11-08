package tga.checkers.security

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tga.checkers.domain.Authority
import tga.checkers.domain.User
import tga.checkers.repository.UserRepository
import java.util.*
import java.util.stream.Collectors

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
class DomainUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    private val log = LoggerFactory.getLogger(DomainUserDetailsService::class.java)

    @Transactional
    override fun loadUserByUsername(login: String): UserDetails {
        log.debug("Authenticating {}", login)
        if (EmailValidator().isValid(login, null)) {
            return userRepository.findOneWithAuthoritiesByEmailIgnoreCase(login)
                .map { user: User -> createSpringSecurityUser(login, user) }
                .orElseThrow { UsernameNotFoundException("User with email $login was not found in the database") }
        }
        val lowercaseLogin = login.toLowerCase(Locale.ENGLISH)
        return userRepository.findOneWithAuthoritiesByLogin(lowercaseLogin)
            .map { user: User -> createSpringSecurityUser(lowercaseLogin, user) }
            .orElseThrow { UsernameNotFoundException("User $lowercaseLogin was not found in the database") }
    }

    private fun createSpringSecurityUser(lowercaseLogin: String, user: User): org.springframework.security.core.userdetails.User {
        if (!user.activated) {
            throw UserNotActivatedException("User $lowercaseLogin was not activated")
        }
        val grantedAuthorities: List<GrantedAuthority> = user.authorities.stream()
            .map { authority: Authority -> SimpleGrantedAuthority(authority.name) }
            .collect(Collectors.toList())

        //todo: provide all other fields to the constructor, to allow Spring-Security make all the checks for us
        return org.springframework.security.core.userdetails.User(user.login,
            user.password,
            grantedAuthorities)
    }
}
