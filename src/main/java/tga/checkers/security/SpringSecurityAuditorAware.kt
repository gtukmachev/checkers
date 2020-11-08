package tga.checkers.security

import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import tga.checkers.config.Constants.SYSTEM_ACCOUNT
import java.util.*

/**
 * Implementation of [AuditorAware] based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> = Optional.of(
        SecurityUtils.currentUserLogin ?: SYSTEM_ACCOUNT
    )
}
