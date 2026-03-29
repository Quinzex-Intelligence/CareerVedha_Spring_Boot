package com.quinzex.config;

import com.quinzex.jwtFilter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter filter) {
        this.jwtFilter = filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        security
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // 🔥 VERY IMPORTANT (preflight fix)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Public APIs
                        .requestMatchers(
                                "/api/spring/login/**",
                                "/api/spring/register**",
                                "/api/spring/registersendotp",
                                "/api/spring/login/send-otp",
                                "/api/spring/refresh",
                                "/api/spring/log-out",
                                "/api/spring/contact",

                                "/api/spring/internal/academics/hierarchy",
                                "/api/spring/get-yt-urls-by-category",
                                "/api/spring/get-exam-categories",
                                "/api/spring/questions-random-category",
                                "/api/spring/questions-random-chapterid",
                                "/api/spring/get-papers/bycategory",
                                "/api/spring/get-questions",
                                "/api/spring/submit-exam",
                                "/api/spring/current-affairs/by-region",
                                "/api/spring/get-all-affairs",

                                "/actuator/**",
                                "/ws/**",
                                "/ads.txt"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔥 FIX: use patterns instead of allowedOrigins
        configuration.setAllowedOriginPatterns(List.of(
                "https://*.careervedha.com"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 🔥 safer for browser + CloudFront
        configuration.setAllowedHeaders(List.of("*"));

        configuration.setExposedHeaders(List.of(
                "Authorization", "Set-Cookie"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}