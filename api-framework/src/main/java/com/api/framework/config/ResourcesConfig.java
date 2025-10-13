package com.api.framework.config;

import com.api.framework.interceptor.RepeatSubmitInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Spring MVC configuration: - Registers global interceptors - Enables static resource and CORS
 * configuration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResourcesConfig implements WebMvcConfigurer {

  /** Interceptor for duplicate submission prevention. */
  private final RepeatSubmitInterceptor repeatSubmitInterceptor;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Example: static upload files or swagger
    registry
        .addResourceHandler("/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
        .setCachePeriod((int) TimeUnit.HOURS.toSeconds(5));

    log.info("✅ Static resource handlers configured successfully.");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    log.info("✅ RepeatSubmitInterceptor registered globally.");
  }

  /** Allow CORS for all origins and methods (customize for production). */
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(true);
    config.setMaxAge(1800L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    log.info("✅ CORS filter initialized (allow all).");
    return new CorsFilter(source);
  }
}
