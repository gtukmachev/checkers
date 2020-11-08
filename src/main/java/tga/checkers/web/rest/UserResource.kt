package tga.checkers.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import tga.checkers.config.Constants
import tga.checkers.domain.User
import tga.checkers.repository.UserRepository
import tga.checkers.security.AuthoritiesConstants
import tga.checkers.service.MailService
import tga.checkers.service.UserService
import tga.checkers.service.dto.UserDTO
import tga.checkers.web.rest.errors.BadRequestAlertException
import tga.checkers.web.rest.errors.EmailAlreadyUsedException
import tga.checkers.web.rest.errors.LoginAlreadyUsedException
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.function.Function
import javax.validation.Valid

/**
 * REST controller for managing users.
 *
 *
 * This class accesses the [User] entity, and needs to fetch its collection of authorities.
 *
 *
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 *
 *
 * We use a View Model and a DTO for 3 reasons:
 *
 *  * We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.
 *  *  Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).
 *  *  As this manages users, for security reasons, we'd rather have a DTO layer.
 *
 *
 *
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
class UserResource(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val mailService: MailService
) {

    private val log = LoggerFactory.getLogger(UserResource::class.java)

    @Value("\${jhipster.clientApp.name}")
    private val applicationName: String? = null

    /**
     * `POST  /users`  : Creates a new user.
     *
     *
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new user, or with status `400 (Bad Request)` if the login or email is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException `400 (Bad Request)` if the login or email is already in use.
     */
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Throws(URISyntaxException::class)
    fun createUser(@RequestBody userDTO: @Valid UserDTO): ResponseEntity<User> {
        log.debug("REST request to save User : {}", userDTO)
        return if (userDTO!!.id != 0L) {
            throw BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists")
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userDTO.login.toLowerCase()).isPresent) {
            throw LoginAlreadyUsedException()
        } else if (userRepository.findOneByEmailIgnoreCase(userDTO.email).isPresent) {
            throw EmailAlreadyUsedException()
        } else {
            val newUser = userService.createUser(userDTO)
            mailService.sendCreationEmail(newUser)
            ResponseEntity.created(URI("/api/users/" + newUser.login))
                .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", newUser.login))
                .body(newUser)
        }
    }

    /**
     * `PUT /users` : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated user.
     * @throws EmailAlreadyUsedException `400 (Bad Request)` if the email is already in use.
     * @throws LoginAlreadyUsedException `400 (Bad Request)` if the login is already in use.
     */
    @PutMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    fun updateUser(@RequestBody userDTO: @Valid UserDTO): ResponseEntity<UserDTO> {
        log.debug("REST request to update User : {}", userDTO)
        var existingUser = userRepository.findOneByEmailIgnoreCase(userDTO!!.email)
        if (existingUser.isPresent && existingUser.get().id != userDTO.id) {
            throw EmailAlreadyUsedException()
        }
        existingUser = userRepository.findOneByLogin(userDTO.login.toLowerCase())
        if (existingUser.isPresent && existingUser.get().id != userDTO.id) {
            throw LoginAlreadyUsedException()
        }
        val updatedUser = userService.updateUser(userDTO)
        return ResponseUtil.wrapOrNotFound(updatedUser,
            HeaderUtil.createAlert(applicationName, "userManagement.updated", userDTO.login))
    }

    /**
     * `GET /users` : get all users.
     *
     * @param pageable the pagination information.
     * @return the [ResponseEntity] with status `200 (OK)` and with body all users.
     */
    @GetMapping("/users")
    fun getAllUsers(pageable: Pageable): ResponseEntity<List<UserDTO>> {
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build()
        }
        val page = userService.getAllManagedUsers(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    private fun onlyContainsAllowedProperties(pageable: Pageable): Boolean {
        return pageable
            .sort
            .stream()
            .map(Sort.Order::getProperty)
            .allMatch { ALLOWED_ORDERED_PROPERTIES.contains(it) }
    }

    /**
     * Gets a list of all roles.
     * @return a string list of all roles.
     */
    @get:PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @get:GetMapping("/users/authorities")
    val authorities: List<String>
        get() = userService.authorities

    /**
     * `GET /users/:login` : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the "login" user, or with status `404 (Not Found)`.
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    fun getUser(@PathVariable login: String): ResponseEntity<UserDTO> {
        log.debug("REST request to get User : {}", login)
        return ResponseUtil.wrapOrNotFound(
            userService.getUserWithAuthoritiesByLogin(login)
                .map { user: User? -> UserDTO(user!!) })
    }

    /**
     * `DELETE /users/:login` : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    fun deleteUser(@PathVariable login: String): ResponseEntity<Void> {
        log.debug("REST request to delete User: {}", login)
        userService.deleteUser(login)
        return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", login)).build()
    }

    companion object {
        private val ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(Arrays.asList("id", "login", "firstName", "lastName", "email", "activated", "langKey"))
    }
}
