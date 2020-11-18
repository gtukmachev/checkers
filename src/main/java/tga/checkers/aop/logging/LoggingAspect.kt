package tga.checkers.aop.logging

import io.github.jhipster.config.JHipsterConstants
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import java.util.*

/**
 * Aspect for logging execution of service and repository Spring components.
 *
 * By default, it only runs with the "dev" profile.
 */
@Aspect
open class LoggingAspect(private val env: Environment) {
    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
        " || within(@org.springframework.stereotype.Service *)" +
        " || within(@org.springframework.web.bind.annotation.RestController *)")
    fun springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(tga.checkers.repository..*)" +
        " || within(tga.checkers.service..*)" +
        " || within(tga.checkers.web.rest..*)" +
        " || within(tga.checkers.web.websocket..*)"
    )
    fun applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Retrieves the [Logger] associated to the given [JoinPoint].
     *
     * @param joinPoint join point we want the logger for.
     * @return [Logger] associated to the given [JoinPoint].
     */
    private fun logger(joinPoint: JoinPoint): Logger {
        return LoggerFactory.getLogger(joinPoint.signature.declaringTypeName)
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice.
     * @param e exception.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    fun logAfterThrowing(joinPoint: JoinPoint, e: Throwable) {
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
            logger(joinPoint)
                .error(
                    "Exception in {}() with cause = \'{}\' and exception = \'{}\'",
                    joinPoint.signature.name,
                    if (e.cause != null) e.cause else "NULL",
                    e.message,
                    e
                )
        } else {
            logger(joinPoint)
                .error(
                    "Exception in {}() with cause = {}",
                    joinPoint.signature.name,
                    if (e.cause != null) e.cause else "NULL"
                )
        }
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice.
     * @return result.
     * @throws Throwable throws [IllegalArgumentException].
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    @Throws(Throwable::class)
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val log = logger(joinPoint)
        if (log.isDebugEnabled) {
            log.debug("Enter: {}() with argument[s] = {}", joinPoint.signature.name, Arrays.toString(joinPoint.args))
        }
        return try {
            val result = joinPoint.proceed()
            if (log.isDebugEnabled) {
                log.debug("Exit: {}() with result = {}", joinPoint.signature.name, result)
            }
            result
        } catch (e: IllegalArgumentException) {
            log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.args), joinPoint.signature.name)
            throw e
        }
    }
}
