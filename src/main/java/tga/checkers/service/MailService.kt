package tga.checkers.service

import io.github.jhipster.config.JHipsterProperties
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import tga.checkers.domain.User
import java.nio.charset.StandardCharsets
import java.util.*
import javax.mail.MessagingException

/**
 * Service for sending emails.
 *
 *
 * We use the [Async] annotation to send emails asynchronously.
 */
@Service
open class MailService(
    private val jHipsterProperties: JHipsterProperties,
    private val javaMailSender: JavaMailSender,
    private val messageSource: MessageSource,
    private val templateEngine: SpringTemplateEngine
) {

    @Async
    open fun sendEmail(to: String, subject: String, content: String, isMultipart: Boolean, isHtml: Boolean) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content)

        // Prepare message using a Spring helper
        val mimeMessage = javaMailSender.createMimeMessage()
        try {
            val message = MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name())
            message.setTo(to)
            message.setFrom(jHipsterProperties.mail.from)
            message.setSubject(subject)
            message.setText(content, isHtml)
            javaMailSender.send(mimeMessage)
            log.debug("Sent email to User '{}'", to)
        } catch (e: MailException) {
            log.warn("Email could not be sent to user '{}'", to, e)
        } catch (e: MessagingException) {
            log.warn("Email could not be sent to user '{}'", to, e)
        }
    }

    @Async
    open fun sendEmailFromTemplate(user: User, templateName: String, titleKey: String) {
        if (user.email == null) {
            log.debug("Email doesn't exist for user '{}'", user.login)
            return
        }
        val locale = Locale.forLanguageTag(user.langKey)
        val context = Context(locale)
        context.setVariable(USER, user)
        context.setVariable(BASE_URL, jHipsterProperties.mail.baseUrl)
        val content = templateEngine.process(templateName, context)
        val subject = messageSource.getMessage(titleKey, null, locale)
        sendEmail(user.email, subject, content, isMultipart = false, isHtml = true)
    }

    @Async
    open fun sendActivationEmail(user: User) {
        log.debug("Sending activation email to '{}'", user.email)
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title")
    }

    @Async
    open fun sendCreationEmail(user: User) {
        log.debug("Sending creation email to '{}'", user.email)
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title")
    }

    @Async
    open fun sendPasswordResetMail(user: User) {
        log.debug("Sending password reset email to '{}'", user.email)
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title")
    }

    companion object {
        private const val USER = "user"
        private const val BASE_URL = "baseUrl"
        private val log = LoggerFactory.getLogger(MailService::class.java)
    }
}
