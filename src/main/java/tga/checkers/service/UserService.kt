package tga.checkers.service

import io.github.jhipster.security.RandomUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tga.checkers.config.Constants
import tga.checkers.domain.Authority
import tga.checkers.domain.User
import tga.checkers.repository.AuthorityRepository
import tga.checkers.repository.UserRepository
import tga.checkers.security.AuthoritiesConstants
import tga.checkers.security.SecurityUtils
import tga.checkers.service.dto.UserDTO
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.HashSet


/**
 * Service class for managing users.
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorityRepository: AuthorityRepository,
    private val cacheManager: CacheManager
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    fun activateRegistration(key: String): Optional<User> {
        log.debug("Activating user for activation key {}", key)
        return userRepository.findOneByActivationKey(key)
            .map { user: User ->
                // activate given user for the registration key.
                user.activated = true
                user.activationKey = null
                clearUserCaches(user)
                log.debug("Activated user: {}", user)
                user
            }
    }

    fun completePasswordReset(newPassword: String, key: String): Optional<User> {
        log.debug("Reset user password for reset key {}", key)
        return userRepository.findOneByResetKey(key)
            .filter { user: User -> user.resetDate.isAfter(Instant.now().minusSeconds(86400)) }
            .map { user: User ->
                user.password = passwordEncoder.encode(newPassword)
                user.resetKey = null
                user.resetDate = null
                clearUserCaches(user)
                user
            }
    }

    fun requestPasswordReset(mail: String): Optional<User> {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter { obj: User -> obj.activated }
            .map { user: User ->
                user.resetKey = RandomUtil.generateResetKey()
                user.resetDate = Instant.now()
                clearUserCaches(user)
                user
            }
    }

    fun registerUser(userDTO: UserDTO, password: String): User {
        val login = userDTO.login.toLowerCase()
        val userOptional = this.userRepository.findOneByLogin(login)

        userOptional.ifPresent{ existingUser: User ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw UsernameAlreadyUsedException()
            }
        }
        userRepository.findOneByEmailIgnoreCase(userDTO.email).ifPresent { existingUser: User ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw EmailAlreadyUsedException()
            }
        }
        val newUser = User()
        val encryptedPassword = passwordEncoder.encode(password)
        newUser.login = userDTO.login.toLowerCase()
        // new user gets initially a generated password
        newUser.password = encryptedPassword
        newUser.firstName = userDTO.firstName
        newUser.lastName = userDTO.lastName
        newUser.email = userDTO.email.toLowerCase()
        newUser.imageUrl = userDTO.imageUrl
        newUser.langKey = userDTO.langKey
        // new user is not active
        newUser.activated = false
        // new user gets registration key
        newUser.activationKey = RandomUtil.generateActivationKey()
        val authorities: MutableSet<Authority> = HashSet()
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent { e: Authority -> authorities.add(e) }
        newUser.authorities = authorities
        userRepository.save(newUser)
        clearUserCaches(newUser)
        log.debug("Created Information for User: {}", newUser)
        return newUser
    }

    private fun removeNonActivatedUser(existingUser: User): Boolean {
        if (existingUser.activated) {
            return false
        }
        userRepository.delete(existingUser)
        userRepository.flush()
        clearUserCaches(existingUser)
        return true
    }

    fun createUser(userDTO: UserDTO): User {
        val user = User()
        user.login = userDTO.login.toLowerCase()
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email.toLowerCase()
        user.imageUrl = userDTO.imageUrl
        if (userDTO.langKey == null) {
            user.langKey = Constants.DEFAULT_LANGUAGE // default language
        } else {
            user.langKey = userDTO.langKey
        }
        val encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword())
        user.password = encryptedPassword
        user.resetKey = RandomUtil.generateResetKey()
        user.resetDate = Instant.now()
        user.activated = true
        val authorities = userDTO.authorities.stream()
            .map { id: String -> authorityRepository.findById(id) }
            .filter { obj: Optional<Authority?> -> obj.isPresent }
            .map { obj: Optional<Authority?> -> obj.get() }
            .collect(Collectors.toSet())
        user.authorities = authorities
        userRepository.save(user)
        clearUserCaches(user)
        log.debug("Created Information for User: {}", user)
        return user
    }


    private fun User.updateFromDto(userDTO: UserDTO): User {
        clearUserCaches(this)

        login = userDTO.login.toLowerCase()
        firstName = userDTO.firstName
        lastName = userDTO.lastName
        email = userDTO.email.toLowerCase()
        imageUrl = userDTO.imageUrl
        activated = userDTO.isActivated
        langKey = userDTO.langKey

        val managedAuthorities = this.authorities
        managedAuthorities.clear()

        userDTO.authorities.stream()
            .map { id: String -> authorityRepository.findById(id) }
            .filter { obj: Optional<Authority?> -> obj.isPresent }
            .map { obj: Optional<Authority?> -> obj.get() }
            .forEach { e: Authority -> managedAuthorities.add(e) }
        clearUserCaches(this)

        log.debug("Changed Information for User: {}", this)

        return this
    }


    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    fun updateUser(userDTO: UserDTO): Optional<UserDTO> {

        val newUserDTO: UserDTO? = userRepository
            .findById(userDTO.id)
            .orElse(null)
            ?.updateFromDto(userDTO)
            ?.let{ UserDTO(it) }

        return Optional.ofNullable( newUserDTO )

    }

    fun deleteUser(login: String) {
        userRepository.findOneByLogin(login).ifPresent { user: User ->
            userRepository.delete(user)
            clearUserCaches(user)
            log.debug("Deleted User: {}", user)
        }
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    fun updateUser(firstName: String, lastName: String, email: String?, langKey: String, imageUrl: String) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap { login: String -> userRepository.findOneByLogin(login) }
            .ifPresent { user: User ->
                user.firstName = firstName
                user.lastName = lastName
                if (email != null) {
                    user.email = email.toLowerCase()
                }
                user.langKey = langKey
                user.imageUrl = imageUrl
                clearUserCaches(user)
                log.debug("Changed Information for User: {}", user)
            }
    }

    @Transactional
    fun changePassword(currentClearTextPassword: String, newPassword: String) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap { login: String -> userRepository.findOneByLogin(login) }
            .ifPresent { user: User ->
                val currentEncryptedPassword = user.password
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw InvalidPasswordException()
                }
                val encryptedPassword = passwordEncoder.encode(newPassword)
                user.password = encryptedPassword
                clearUserCaches(user)
                log.debug("Changed password for User: {}", user)
            }
    }

    @Transactional(readOnly = true)
    fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map { user: User -> UserDTO(user) }
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthoritiesByLogin(login: String): Optional<User> {
        return userRepository.findOneWithAuthoritiesByLogin(login)
    }

    @get:Transactional(readOnly = true)
    val userWithAuthorities: Optional<User>
        get() = SecurityUtils.getCurrentUserLogin().flatMap { login: String -> userRepository.findOneWithAuthoritiesByLogin(login) }

    /**
     * Not activated users should be automatically deleted after 3 days.
     *
     *
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    fun removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(Consumer { user: User ->
                log.debug("Deleting not activated user {}", user.login)
                userRepository.delete(user)
                clearUserCaches(user)
            })
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    @get:Transactional(readOnly = true)
    val authorities: List<String>
        get() = authorityRepository.findAll().stream().map { obj: Authority -> obj.name }.collect(Collectors.toList())

    private fun clearUserCaches(user: User) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.login)
        if (user.email != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.email)
        }
    }
}
