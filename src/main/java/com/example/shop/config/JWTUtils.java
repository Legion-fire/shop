package com.example.shop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
public class JWTUtils {

    private final SecretKey secretKey;
    private static final long EXPIRATION_TIME = Duration.ofHours(24).toMillis(); // access
    private static final long REFRESH_EXPIRATION_TIME = Duration.ofDays(7).toMillis();

    public JWTUtils(@Value("${security.jwt.secret}") String secretBase64) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretBase64);
        this.secretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList());
        return buildToken(claims, userDetails.getUsername(), EXPIRATION_TIME);
    }

    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        if (claims == null) claims = new HashMap<>();
        claims.putIfAbsent("type", "refresh");
        return buildToken(claims, userDetails.getUsername(), REFRESH_EXPIRATION_TIME);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parse(token);
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String type = extractClaim(token, c -> c.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private String buildToken(Map<String, Object> claims, String subject, long ttl) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttl))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
