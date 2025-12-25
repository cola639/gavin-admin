package com.api.config;

import com.api.framework.security.filter.JwtAuthenticationTokenFilter;
import com.api.framework.security.handle.AuthenticationEntryPointImpl;
import com.api.framework.security.handle.LogoutSuccessHandlerImpl;
import com.api.system.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String[] PUBLIC_POST_ENDPOINTS = {"/login", "/register", "/captchaImage"};
  private static final String[] PUBLIC_ENDPOINTS = {"/auth/**", "/error"};

  private static final String[] SWAGGER_ENDPOINTS = {
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/swagger-resources/**",
    "/webjars/**",
    "/*/api-docs",
    "/v3/api-docs/**",
    "/druid/**"
  };

  private final AuthenticationEntryPointImpl unauthorizedHandler;
  private final LogoutSuccessHandlerImpl logoutSuccessHandler;
  private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
  private final CorsFilter corsFilter;

  // ✅ Inject by concrete type to avoid ambiguity if you have other handlers
  private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  /**
   * (1) OAuth2 filter chain: - MUST allow session (OAuth2 "state" uses it) - Only matches OAuth2
   * endpoints
   */
  @Bean
  @Order(1)
  public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/oauth2/**", "/login/oauth2/**")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .addFilterBefore(corsFilter, LogoutFilter.class)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .authorizationEndpoint(a -> a.baseUri("/oauth2/authorization"))
                    .redirectionEndpoint(r -> r.baseUri("/login/oauth2/code/*"))
                    .successHandler(oauth2LoginSuccessHandler))
        .build();
  }

  /** (2) API filter chain: - Stateless JWT for everything else */
  @Bean
  @Order(2)
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .headers(
            headers ->
                headers
                    .frameOptions(frame -> frame.sameOrigin())
                    .cacheControl(cache -> cache.disable()))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // preflight
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()

                    // static resources
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()

                    // public endpoints
                    .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS)
                    .permitAll()
                    .requestMatchers(PUBLIC_ENDPOINTS)
                    .permitAll()

                    // swagger
                    .requestMatchers(SWAGGER_ENDPOINTS)
                    .permitAll()

                    // everything else => JWT
                    .anyRequest()
                    .authenticated())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
        .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
        .addFilterBefore(corsFilter, LogoutFilter.class)
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .build();
  }
}
