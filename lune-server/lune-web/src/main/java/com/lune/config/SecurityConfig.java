package com.lune.config;

import com.lune.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final com.lune.security.RateLimitFilter rateLimitFilter;
    private final com.lune.security.SecurityHeadersFilter securityHeadersFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          com.lune.security.RateLimitFilter rateLimitFilter,
                          com.lune.security.SecurityHeadersFilter securityHeadersFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.securityHeadersFilter = securityHeadersFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 安全响应头 + 限流 在认证之前生效
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/user/profile/*").permitAll()
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/articles/**", "/api/categories/**",
                    "/api/comments/**", "/api/essays/**", "/api/records/**", "/api/treeholes/**",
                    "/api/diaries/**", "/api/family/**", "/api/site-config/**",
                    "/api/resume/**", "/api/wishes/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/articles/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/comments/**", "/api/treeholes/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/wishes/**").authenticated()
                .requestMatchers("/upload/**").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .requestMatchers("/api/visit/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
