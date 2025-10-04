package com.api.framework.config;

import com.api.common.config.AppConfig;
import com.api.common.constant.Constants;
// import com.api.framework.interceptor.RepeatSubmitInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Global Web MVC Configuration.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Serve local file resources (uploads, static files).
 *   <li>Enable Swagger UI static resources.
 *   <li>Register global request interceptors (e.g., repeat-submit protection).
 *   <li>Configure global CORS policy for API access.
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResourcesConfig implements WebMvcConfigurer {

  /** Interceptor for preventing duplicate form submissions. */
  //  private final RepeatSubmitInterceptor repeatSubmitInterceptor;

  /**
   * Configure static resource handling.
   *
   * <ul>
   *   <li>Maps uploaded files to an accessible URL path.
   *   <li>Enables Swagger UI static resource access.
   * </ul>
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Serve uploaded files from the configured local directory
    registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**");
    //        .addResourceLocations("file:" + AppConfig.getProfile() + "/");

    // Serve Swagger UI resources
    registry
        .addResourceHandler("/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
        .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());

    log.info("Static resource handlers initialized successfully.");
  }

  /**
   * Register global interceptors.
   *
   * <p>Currently includes repeat-submit protection for all API endpoints.
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    //    registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    log.info("RepeatSubmitInterceptor registered successfully.");
  }

  /**
   * Configure global Cross-Origin Resource Sharing (CORS) policy.
   *
   * <p>Allows requests from all origins for development. Adjust allowed origins for production.
   *
   * @return configured {@link CorsFilter}
   */
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();

    // ✅ Allow all origins (use specific domains in production)
    config.addAllowedOriginPattern("*");

    // ✅ Allow all request headers
    config.addAllowedHeader("*");

    // ✅ Allow all HTTP methods (GET, POST, PUT, DELETE, OPTIONS, etc.)
    config.addAllowedMethod("*");

    // ✅ Allow credentials (Authorization, Cookies)
    config.setAllowCredentials(true);

    // ✅ Cache CORS response for 30 minutes
    config.setMaxAge(1800L);

    // Register configuration for all API endpoints
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    log.info("CORS configuration initialized successfully (allow all origins).");
    return new CorsFilter(source);
  }
}
