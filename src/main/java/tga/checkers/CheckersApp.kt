package tga.checkers

import io.github.jhipster.config.DefaultProfileUtil
import io.github.jhipster.config.JHipsterConstants
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.env.Environment
import tga.checkers.config.ApplicationProperties
import java.net.InetAddress
import java.net.UnknownHostException
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableConfigurationProperties(LiquibaseProperties::class, ApplicationProperties::class)
class CheckersApp(
    private val env: Environment
) {
    /**
     * Initializes checkers.
     *
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     *
     * You can find more information on how profiles work with JHipster on [https://www.jhipster.tech/profiles/](https://www.jhipster.tech/profiles/).
     */
    @PostConstruct
    fun initApplication() {
        val ap = env.activeProfiles
        if (
            ap.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
            ap.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        ) log.error("You have misconfigured your application! It should not run with both the 'dev' and 'prod' profiles at the same time.")

        if (ap.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
            ap.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)
        ) log.error("You have misconfigured your application! It should not run with both the 'dev' and 'cloud' profiles at the same time.")
    }

    companion object {
        private val log = LoggerFactory.getLogger(CheckersApp::class.java)

        /**
         * Main method, used to run the application.
         *
         * @param args the command line arguments.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication(CheckersApp::class.java)
            DefaultProfileUtil.addDefaultProfile(app)
            val env: Environment = app.run(*args).environment
            logApplicationStartup(env)
        }

        private fun logApplicationStartup(env: Environment) {
            var protocol = when(env.getProperty("server.ssl.key-store")) {
                    null -> "http"
                    else -> "https"
                }
            val serverPort  = env.getProperty("server.port")
            val contextPath = env.getProperty("server.servlet.context-path").let {
                if (it.isNullOrBlank()) "/" else it
            }

            val hostAddress: String = try {
                    InetAddress.getLocalHost().hostAddress
                } catch (e: UnknownHostException) {
                    log.warn("The host name could not be determined, using `localhost` as fallback")
                    "localhost"
                }

            val appName = env.getProperty("spring.application.name")

            log.info("""
                    ----------------------------------------------------------
                        Application '$appName' is running! Access URLs:
                        Local:      $protocol://localhost:$serverPort$contextPath
                        External:   $protocol://$hostAddress:$serverPort$contextPath
                        Profile(s): ${env.activeProfiles}
                    ----------------------------------------------------------
                """.trimIndent()
            )
        }
    }
}
