package com.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long accessTokenExpirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long accessTokenExpirationMs) {
        if (secret==null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateToken(String subject){
        Date now =new Date();
        Date expire=new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token);
            return true;
        }catch (JwtException ex){
            return false;
        }
    }

    public String getUsernameFromToken(String token){
        Claims claims =Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public Date getExpirationDateFromToken(String token){
        Claims claims =Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }
}
