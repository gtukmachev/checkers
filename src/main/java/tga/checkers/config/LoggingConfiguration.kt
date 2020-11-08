package tga.checkers.config

import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.config.logging.LoggingUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/*
* Configures the console and Logstash log appenders from the app properties
*/
@Configuration
class LoggingConfiguration(@Value("\${spring.application.name}") appName: String,
                           @Value("\${server.port}") serverPort: String,
                           jHipsterProperties: JHipsterProperties,
                           mapper: ObjectMapper) {
    init {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext

        val customFields = """{"app_name":"$appName","app_port":"$serverPort"}"""

        val loggingProperties = jHipsterProperties.logging
        val logstashProperties = loggingProperties.logstash
        if (loggingProperties.isUseJsonFormat) {
            LoggingUtils.addJsonConsoleAppender(context, customFields)
        }
        if (logstashProperties.isEnabled) {
            LoggingUtils.addLogstashTcpSocketAppender(context, customFields, logstashProperties)
        }
        if (loggingProperties.isUseJsonFormat || logstashProperties.isEnabled) {
            LoggingUtils.addContextListener(context, customFields, loggingProperties)
        }
        if (jHipsterProperties.metrics.logs.isEnabled) {
            LoggingUtils.setMetricsMarkerLogbackFilter(context, loggingProperties.isUseJsonFormat)
        }
    }
}
