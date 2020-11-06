package tga.checkers.repository

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tga.checkers.domain.User
import java.time.Instant
import java.util.*

/**
 * Spring Data JPA repository for the [User] entity.
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    companion object {
        const val USERS_BY_LOGIN_CACHE = "usersByLogin"
        const val USERS_BY_EMAIL_CACHE = "usersByEmail"
    }

    fun findOneByActivationKey(activationKey: String): Optional<User>
    fun findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(dateTime: Instant): List<User>
    fun findOneByResetKey(resetKey: String): Optional<User>
    fun findOneByEmailIgnoreCase(email: String): Optional<User>
    fun findOneByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = ["authorities"])
    @Cacheable(cacheNames = [USERS_BY_LOGIN_CACHE])
    fun findOneWithAuthoritiesByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = ["authorities"])
    @Cacheable(cacheNames = [USERS_BY_EMAIL_CACHE])
    fun findOneWithAuthoritiesByEmailIgnoreCase(email: String): Optional<User>
    fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>

}
