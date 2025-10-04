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
public class JwtAuthenticationFilter extends OncePerRequestFilter{
  
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
            if (jwt != null && jwtUtils.validateJwtToken(jwt)){
                String email = jwtUtils.getEmailFromJwtToken(jwt);
                UserDetailsImpl userDetails = (UserDetailsImpl) customUserDetailsServices.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e){
            // Optionally log the exception
        }
        filterChain.doFilter(request, response);
      }

      private String parseJwt(HttpServletRequest request) {
          String headerAuth = request.getHeader("Authorization");
          if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
              return headerAuth.substring(7);
          }
          return null;
      }
  
}
