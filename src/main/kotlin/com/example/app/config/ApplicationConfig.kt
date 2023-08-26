package com.example.app.config

import com.example.app.FrpConfigManager
import com.example.app.FrpConfigManagerImpl
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.ErrorPageRegistrar
import org.springframework.boot.web.server.ErrorPageRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.util.*

class ApplicationConfig {
}

@Configuration
class BeanFactory {
    @Bean
    fun frpConfigManager(): FrpConfigManager {
        val property = System.getProperty("config.file")
        property?.let {
            return FrpConfigManagerImpl(it)
        } ?: throw RuntimeException("config.file not set")
    }
}


@Configuration
class RequestCorsFilter {
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.setAllowedOriginPatterns(
            listOf(
                "http://localhost:[*]",
                "http://192.168.31.*:[*]",
                "https://192.168.31.*:[*]",
                "https://*.wenchao.fit:[*]",
                "http://*.wenchao.fit:[*]",
            )
        );
        config.allowedHeaders = Arrays.asList("Origin", "Content-Type", "Accept", "responseType", "Authorization")
        config.allowedMethods = Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}


@Component
class StaticServerErrorPageConfig : ErrorPageRegistrar {
    override fun registerErrorPages(registry: ErrorPageRegistry) {
        val error404Page = ErrorPage(HttpStatus.NOT_FOUND, "/index.html")
        registry.addErrorPages(error404Page)
    }
}

