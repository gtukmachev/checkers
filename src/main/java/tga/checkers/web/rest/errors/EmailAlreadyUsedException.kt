package tga.checkers.web.rest.errors

class EmailAlreadyUsedException : BadRequestAlertException(
    ErrorConstants.EMAIL_ALREADY_USED_TYPE,
    "Email is already in use!",
    "userManagement",
    "emailexists"
)
