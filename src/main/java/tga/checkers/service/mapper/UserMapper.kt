package tga.checkers.service.mapper

import org.springframework.stereotype.Service
import tga.checkers.domain.Authority
import tga.checkers.domain.User
import tga.checkers.service.dto.UserDTO
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashSet

/**
 * Mapper for the entity [User] and its DTO called [UserDTO].
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
class UserMapper {
    fun userToUserDTO(user: User): UserDTO = UserDTO(user)

    fun usersToUserDTOs(users: List<User>): List<UserDTO> = users.map(::userToUserDTO)

    fun userDTOToUser(userDTO: UserDTO): User {
        return User().apply {
            id = userDTO.id
            login = userDTO.login
            firstName = userDTO.firstName
            lastName = userDTO.lastName
            email = userDTO.email
            imageUrl = userDTO.imageUrl
            activated = userDTO.isActivated
            langKey = userDTO.langKey
            authorities = authoritiesFromStrings(userDTO.authorities)
        }
    }

    fun userDTOsToUsers(userDTOs: List<UserDTO>): List<User> = userDTOs.map(::userDTOToUser)

//    fun userFromId(id: Long): User =  User().apply { this.id = id }

    private fun authoritiesFromStrings(authoritiesAsString: Set<String>): MutableSet<Authority> {
        return authoritiesAsString.asSequence()
            .map{ st -> Authority().apply{ name = st } }
            .toMutableSet()
    }
}
