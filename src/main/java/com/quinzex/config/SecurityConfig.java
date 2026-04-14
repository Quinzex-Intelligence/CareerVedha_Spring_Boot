package com.quinzex.config;

import com.quinzex.jwtFilter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    public SecurityConfig(JwtFilter Filter){
        this.jwtFilter=Filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws  Exception{
        security.csrf(AbstractHttpConfigurer::disable).cors(cors->cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->auth.requestMatchers("/api/spring/internal/academics/hierarchy","/api/spring/get-yt-urls-by-category","/actuator/**","/api/spring/get-exam-categories","/api/spring/registersendotp","/api/spring/questions-random-category","/api/spring/questions-random-chapterid","/api/spring/registeruser","/api/spring/login","/ads.txt","/api/spring/login/send-otp","/api/spring/refresh","/api/spring/log-out","/api/spring/contact", "/api/spring/get-papers/bycategory","/api/spring/get-questions","/api/spring/submit-exam", "/api/spring/current-affairs/by-region","/api/spring/get-all-affairs", "/ws/**","/api/spring/services/get/**").permitAll().anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return security.build();
    }
@Bean
    public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(List.of("https://www.careervedha.com","https://careervedha.com","https://api.careervedha.com","http://localhost:3000"));
      configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
      configuration.setAllowedHeaders(List.of("*"));
      configuration.setExposedHeaders(List.of( "Authorization","Set-Cookie"));
      configuration.setAllowCredentials(true);

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
    }
}
