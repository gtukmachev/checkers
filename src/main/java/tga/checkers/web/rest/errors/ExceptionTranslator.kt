package tga.checkers.web.rest.errors

import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.web.util.HeaderUtil
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.dao.DataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.*
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait
import org.zalando.problem.violations.ConstraintViolationProblem
import tga.checkers.service.UsernameAlreadyUsedException
import java.net.URI
import java.util.*
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
class ExceptionTranslator(private val env: Environment) : ProblemHandling, SecurityAdviceTrait {

    companion object {
        private const val FIELD_ERRORS_KEY = "fieldErrors"
        private const val MESSAGE_KEY = "message"
        private const val PATH_KEY = "path"
        private const val VIOLATIONS_KEY = "violations"
    }

    @Value("\${jhipster.clientApp.name}")
    private val applicationName: String? = null

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    override fun process(entity: ResponseEntity<Problem>, request: NativeWebRequest): ResponseEntity<Problem> {

        val problem = entity.body
        if (!(problem is ConstraintViolationProblem || problem is DefaultProblem)) {
            return entity
        }

        val builder = Problem.builder()
            .withType(if (Problem.DEFAULT_TYPE == problem.type) ErrorConstants.DEFAULT_TYPE else problem.type)
            .withStatus(problem.status)
            .withTitle(problem.title)
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest::class.java).requestURI)

        if (problem is ConstraintViolationProblem) {
            builder
                .with(VIOLATIONS_KEY, problem.violations)
                .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)

        } else {
            builder
                .withCause((problem as DefaultProblem).cause)
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance())

            problem.getParameters()
                .forEach { (key: String, value: Any?) -> builder.with(key, value) }

            if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.getStatus()!!.statusCode)
            }
        }
        return ResponseEntity(builder.build(), entity.headers, entity.statusCode)
    }

    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, request: NativeWebRequest): ResponseEntity<Problem> {
        val result = ex.bindingResult

        val fieldErrors = result.fieldErrors.stream()
            .map { f: FieldError -> FieldErrorVM(f.objectName.replaceFirst("DTO$".toRegex(), ""), f.field, f.code) }
            .collect(Collectors.toList())

        val problem: Problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler
    fun handleEmailAlreadyUsedException(ex: tga.checkers.service.EmailAlreadyUsedException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = EmailAlreadyUsedException()
        return create(problem, request, HeaderUtil.createFailureAlert(applicationName, true, problem.entityName, problem.errorKey, problem.message))
    }

    @ExceptionHandler
    fun handleUsernameAlreadyUsedException(ex: UsernameAlreadyUsedException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = LoginAlreadyUsedException()
        return create(problem, request, HeaderUtil.createFailureAlert(applicationName, true, problem.entityName, problem.errorKey, problem.message))
    }

    @ExceptionHandler
    fun handleInvalidPasswordException(ex: tga.checkers.service.InvalidPasswordException, request: NativeWebRequest): ResponseEntity<Problem> {
        return create(InvalidPasswordException(), request)
    }

    @ExceptionHandler
    fun handleBadRequestAlertException(ex: BadRequestAlertException, request: NativeWebRequest): ResponseEntity<Problem> {
        return create(ex, request, HeaderUtil.createFailureAlert(applicationName, true, ex.entityName, ex.errorKey, ex.message))
    }

    @ExceptionHandler
    fun handleConcurrencyFailure(ex: ConcurrencyFailureException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem: Problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with(MESSAGE_KEY, ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build()
        return create(ex, problem, request)
    }

    override fun prepare(throwable: Throwable, status: StatusType, type: URI): ProblemBuilder {
        val activeProfiles: Collection<String> = listOf(*env.activeProfiles)
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            if (throwable is HttpMessageConversionException) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.reasonPhrase)
                    .withStatus(status)
                    .withDetail("Unable to convert http message")
                    .withCause(Optional.ofNullable(throwable.cause)
                        .filter { cause: Throwable? -> isCausalChainsEnabled }
                        .map { throwable: Throwable? -> this.toProblem(throwable) }
                        .orElse(null))
            }
            if (throwable is DataAccessException) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.reasonPhrase)
                    .withStatus(status)
                    .withDetail("Failure during data access")
                    .withCause(Optional.ofNullable(throwable.cause)
                        .filter { isCausalChainsEnabled }
                        .map { err: Throwable -> this.toProblem(err) }
                        .orElse(null))
            }
            if (containsPackageName(throwable.message)) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.reasonPhrase)
                    .withStatus(status)
                    .withDetail("Unexpected runtime exception")
                    .withCause(Optional.ofNullable(throwable.cause)
                        .filter { cause: Throwable? -> isCausalChainsEnabled }
                        .map { throwable: Throwable? -> this.toProblem(throwable) }
                        .orElse(null))
            }
        }
        return Problem.builder()
            .withType(type)
            .withTitle(status.reasonPhrase)
            .withStatus(status)
            .withDetail(throwable.message)
            .withCause(Optional.ofNullable(throwable.cause)
                .filter { cause: Throwable? -> isCausalChainsEnabled }
                .map { err: Throwable? -> this.toProblem(err) }
                .orElse(null))
    }

    private fun containsPackageName(message: String?): Boolean {
        if (message == null) return false
        // This list is for sure not complete
        return StringUtils.containsAny(message, "org.", "java.", "net.", "javax.", "com.", "io.", "de.", "tga.checkers")
    }

}
