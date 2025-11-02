package com.work.IGA.Configuration;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsServices customUserDetailsServices;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Auth Header: " + request.getHeader("Authorization"));
            
            if (jwt != null) {
                System.out.println("JWT token found: " + jwt.substring(0, Math.min(10, jwt.length())) + "...");
                if (jwtUtils.validateJwtToken(jwt)) {
                    String email = jwtUtils.getEmailFromJwtToken(jwt);
                    System.out.println("Email from token: " + email);
                    
                    UserDetailsImpl userDetails = (UserDetailsImpl) customUserDetailsServices.loadUserByUsername(email);
                    System.out.println("User loaded with roles: " + userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication successful for user: " + email);
                } else {
                    System.out.println("JWT token validation failed");
                }
            } else {
                System.out.println("No JWT token found in request headers");
            }
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        System.out.println("Raw Authorization header: " + headerAuth);
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            System.out.println("Extracted token length: " + token.length());
            return token;
        }
        return null;
    }
}