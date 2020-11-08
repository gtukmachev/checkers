package tga.checkers.config

import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.JHipsterProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit

@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
class StaticResourcesWebConfiguration(
    private val jhipsterProperties: JHipsterProperties
) : WebMvcConfigurer {

    companion object {
        val RESOURCE_LOCATIONS = arrayOf(
            "classpath:/static/app/",
            "classpath:/static/content/",
            "classpath:/static/i18n/"
        )

        val RESOURCE_PATHS = arrayOf(
            "/app/*",
            "/content/*",
            "/i18n/*"
        )
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val resourceHandlerRegistration = appendResourceHandler(registry)
        initializeResourceHandler(resourceHandlerRegistration)
    }

    protected fun appendResourceHandler(registry: ResourceHandlerRegistry): ResourceHandlerRegistration {
        return registry.addResourceHandler(*RESOURCE_PATHS)
    }

    protected fun initializeResourceHandler(resourceHandlerRegistration: ResourceHandlerRegistration) {
        resourceHandlerRegistration.addResourceLocations(*RESOURCE_LOCATIONS).setCacheControl(cacheControl)
    }

    protected val cacheControl: CacheControl
        get() = CacheControl.maxAge(jHipsterHttpCacheProperty.toLong(), TimeUnit.DAYS).cachePublic()

    private val jHipsterHttpCacheProperty: Int
        get() = jhipsterProperties.http.cache.timeToLiveInDays

}
