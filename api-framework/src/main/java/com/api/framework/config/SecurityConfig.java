package com.api.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 允许所有请求，不需要登录
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // 关闭 CSRF（表单跨站攻击防护），否则 POST 请求可能报 403
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
