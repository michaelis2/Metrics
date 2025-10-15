package com.server.server.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMs;

    public JwtUtil(@Value(value="${jwt.secret}") String secret, @Value(value="${jwt.expirationMs}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor((byte[])secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + this.expirationMs);
        return ((JwtBuilder)((JwtBuilder)((JwtBuilder)Jwts.builder().setSubject(username)).claim("role", (Object)role).setIssuedAt(now)).setExpiration(expiry)).signWith(this.key, SignatureAlgorithm.HS256).compact();
    }

    public String extractUsername(String token) {
        return ((Claims)Jwts.parser().setSigningKey(this.key).build().parseClaimsJws((CharSequence)token).getBody()).getSubject();
    }

    public String extractRole(String token) {
        return (String)((Claims)Jwts.parser().setSigningKey(this.key).build().parseClaimsJws((CharSequence)token).getBody()).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(this.key).build().parseClaimsJws((CharSequence)token);
            return true;
        }
        catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

