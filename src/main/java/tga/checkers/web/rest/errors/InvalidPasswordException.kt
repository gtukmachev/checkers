package tga.checkers.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status
import org.zalando.problem.ThrowableProblem
import java.lang.RuntimeException

class InvalidPasswordException : AbstractThrowableProblem(
    ErrorConstants.INVALID_PASSWORD_TYPE,
    "Incorrect password",
    Status.BAD_REQUEST
) {
    override fun getCause(): Exceptional? = null // it always null (see the super constructor parameters)
}
