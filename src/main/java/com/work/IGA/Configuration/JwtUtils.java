package com.work.IGA.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;


@Component
public class JwtUtils {
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    
    private SecretKey secretKey;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @PostConstruct
    public void init() {
        // Use the configured secret instead of generating a new one
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        System.out.println("JWT Secret Key initialized successfully");
    }

    public String generateJwToken(UserDetailsImpl userDetails) {
        System.out.println("Generating token for user: " + userDetails.getUsername());
        System.out.println("User roles: " + userDetails.getAuthorities());
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date((new java.util.Date()).getTime() + jwtExpirationMs))
                .claim("id", userDetails.getId().toString())
                .claim("role", userDetails.getAuthorities().stream().map(Object::toString).toList())
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            System.out.println("Validating JWT token: " + token.substring(0, Math.min(10, token.length())) + "...");
            var claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            System.out.println("Token validation successful");
            System.out.println("Token claims: " + claims);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            System.out.println("JWT validation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
 
