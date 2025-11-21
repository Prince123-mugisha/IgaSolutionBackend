package com.work.IGA.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new RuntimeException("JWT secret is not configured. Please set app.jwtSecret property.");
        }
        
        // Ensure the secret is at least 384 bits (48 bytes) for HS384
        if (jwtSecret.length() < 48) {
            throw new RuntimeException("JWT secret must be at least 48 characters for HS384. Current length: " + jwtSecret.length());
        }
        
        try {
            secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            System.out.println("=== JWT SECRET KEY INITIALIZED ===");
            System.out.println("Secret length: " + jwtSecret.length() + " characters");
            System.out.println("Secret first 10 chars: " + jwtSecret.substring(0, Math.min(10, jwtSecret.length())) + "...");
            System.out.println("Secret hash code: " + jwtSecret.hashCode());
            System.out.println("==================================");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT secret key", e);
        }
    }

    public String generateJwToken(UserDetailsImpl userDetails) {
        System.out.println("=== GENERATING JWT TOKEN ===");
        System.out.println("User: " + userDetails.getUsername());
        System.out.println("User ID: " + userDetails.getId());
        System.out.println("User roles: " + userDetails.getAuthorities());
        System.out.println("Secret hash being used: " + jwtSecret.hashCode());
        
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date((new java.util.Date()).getTime() + jwtExpirationMs))
                .claim("id", userDetails.getId().toString())
                .claim("role", userDetails.getAuthorities().stream().map(Object::toString).toList())
                .signWith(secretKey, io.jsonwebtoken.SignatureAlgorithm.HS384)
                .compact();
        
        System.out.println("Token generated successfully");
        System.out.println("Token length: " + token.length());
        System.out.println("============================");
        return token;
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
            System.out.println("=== VALIDATING JWT TOKEN ===");
            System.out.println("Token (first 20 chars): " + token.substring(0, Math.min(20, token.length())) + "...");
            System.out.println("Token length: " + token.length());
            System.out.println("Secret hash being used: " + jwtSecret.hashCode());
            System.out.println("Secret length: " + jwtSecret.length());
            
            var claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .setAllowedClockSkewSeconds(300) // 5 minutes clock skew allowance
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            System.out.println("✓ TOKEN VALIDATION SUCCESSFUL");
            System.out.println("User: " + claims.getSubject());
            System.out.println("Issued at: " + claims.getIssuedAt());
            System.out.println("Expires at: " + claims.getExpiration());
            System.out.println("============================");
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.err.println("✗ JWT SIGNATURE VALIDATION FAILED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Secret hash used for validation: " + jwtSecret.hashCode());
            System.err.println("");
            System.err.println("POSSIBLE CAUSES:");
            System.err.println("1. Token was generated with a different JWT_SECRET");
            System.err.println("2. Application was restarted and JWT_SECRET changed");
            System.err.println("3. Token is from a different environment (dev/prod)");
            System.err.println("");
            System.err.println("SOLUTION: Login again to get a new token with current secret");
            System.err.println("============================");
            return false;
        } catch (ExpiredJwtException e) {
            System.err.println("✗ JWT TOKEN EXPIRED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Expired at: " + e.getClaims().getExpiration());
            System.err.println("SOLUTION: Login again to get a new token");
            System.err.println("============================");
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("✗ JWT TOKEN UNSUPPORTED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("============================");
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("✗ JWT TOKEN INVALID");
            System.err.println("Error: " + e.getMessage());
            System.err.println("============================");
            return false;
        } catch (Exception e) {
            System.err.println("✗ JWT VALIDATION ERROR");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("============================");
            return false;
        }
    }
}