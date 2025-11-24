package com.work.IGA.Configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsServices customUserDetailsServices;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final CorsConfigurationSource corsConfigSource;

    public SecurityConfig(
        CustomUserDetailsServices customUserDetailsServices,
        JwtAuthenticationEntryPoint unauthorizedHandler,
        JwtUtils jwtUtils,
        @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigSource
    ) {
        this.customUserDetailsServices = customUserDetailsServices;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
        this.corsConfigSource = corsConfigSource;
    }

    @Bean
    public JwtAuthenticationFilter authenticationJwtTokenFilter() {
        return new JwtAuthenticationFilter(jwtUtils, customUserDetailsServices);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

   @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigSource))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth ->
            auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/v1/student/**").hasAuthority("ROLE_STUDENT")
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/v1/instructor/**").hasAuthority("ROLE_INSTRUCTOR")
                .requestMatchers("/api/v1/modules/**").hasAuthority("ROLE_INSTRUCTOR")
                .requestMatchers("/api/v1/resources/**").hasAuthority("ROLE_INSTRUCTOR")
                .requestMatchers("/api/v1/assignment/**").hasAuthority("ROLE_INSTRUCTOR")

                // Public courses endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/courses/all").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/courses/{courseId}").permitAll()

                // Student-specific courses
                .requestMatchers(HttpMethod.POST, "/api/v1/courses/rate/{courseId}").hasAuthority("ROLE_STUDENT")

                // Instructor-specific courses
                .requestMatchers(HttpMethod.POST, "/api/v1/courses/create").hasAuthority("ROLE_INSTRUCTOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/courses/update/{courseId}").hasAuthority("ROLE_INSTRUCTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/delete/{courseId}").hasAuthority("ROLE_INSTRUCTOR")

                // Payments
                .requestMatchers("/api/public/payments/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/students/payments/verify/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/students/payments/reference/**").permitAll()

                .anyRequest().authenticated()
        );

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
}



}