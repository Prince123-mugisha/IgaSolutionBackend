package com.work.IGA.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
        // Generate a secure key for HS512
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    public String generateJwToken(UserDetailsImpl userDetails) {
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
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
 
