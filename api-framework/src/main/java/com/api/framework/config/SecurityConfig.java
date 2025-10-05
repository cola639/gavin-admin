package com.api.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.api.framework.security.filter.JwtAuthenticationTokenFilter;
import com.api.framework.security.handle.AuthenticationEntryPointImpl;
import com.api.framework.security.handle.LogoutSuccessHandlerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring Security configuration for JWT-based authentication.
 *
 * <p>Features: - Stateless authentication (no session) - Custom authentication entry point - JWT
 * token validation filter - Configurable list of public URLs - Strong BCrypt password encoding
 */
@Slf4j
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

  /** Custom user authentication logic */
  private final UserDetailsService userDetailsService;

  /** Authentication failure handler */
  private final AuthenticationEntryPointImpl unauthorizedHandler;

  /** Logout success handler */
  private final LogoutSuccessHandlerImpl logoutSuccessHandler;

  /** JWT token authentication filter */
  private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

  /** Cross-origin resource sharing filter */
  private final CorsFilter corsFilter;

  /** List of URLs that can be accessed anonymously */
  //  private final PermitAllUrlProperties permitAllUrlProperties;

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /** BCrypt password encoder for secure password hashing. */
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** Define the main Spring Security filter chain. */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // Disable CSRF (since we’re stateless)
        .csrf(csrf -> csrf.disable())

        // Configure HTTP headers (allow same-origin iframes for H2/Swagger)
        .headers(
            headers ->
                headers
                    .cacheControl(cache -> cache.disable())
                    .frameOptions(options -> options.sameOrigin()))

        // Handle authentication exceptions globally
        .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))

        // No session creation (stateless JWT mode)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Authorization rules
        .authorizeHttpRequests(
            requests -> {

              // Open login/register/captcha endpoints
              requests.requestMatchers("/login", "/register", "/captchaImage").permitAll();

              // Open all /auth/** endpoints
              requests.requestMatchers("/auth/**").permitAll();

              // Allow static resources
              requests
                  .requestMatchers(
                      HttpMethod.GET,
                      "/",
                      "/*.html",
                      "/**.html",
                      "/**.css",
                      "/**.js",
                      "/profile/**")
                  .permitAll();

              // Allow Swagger & monitoring tools
              requests
                  .requestMatchers(
                      "/swagger-ui.html",
                      "/swagger-resources/**",
                      "/webjars/**",
                      "/*/api-docs",
                      "/druid/**")
                  .permitAll();

              // Everything else requires authentication
              requests.anyRequest().authenticated();
            })

        // Configure logout behavior
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))

        // Add JWT filter before username/password filter
        .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)

        // Add CORS filter before logout & JWT filters
        .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
        .addFilterBefore(corsFilter, LogoutFilter.class)
        .build();
  }
}
