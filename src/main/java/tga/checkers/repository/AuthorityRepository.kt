package tga.checkers.repository

import org.springframework.data.jpa.repository.JpaRepository
import tga.checkers.domain.Authority

/**
 * Spring Data JPA repository for the [Authority] entity.
 */
interface AuthorityRepository : JpaRepository<Authority, String>
