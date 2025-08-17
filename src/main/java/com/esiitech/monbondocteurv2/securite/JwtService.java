package com.esiitech.monbondocteurv2.securite;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // ✅ Clé secrète de 256 bits recommandée (doit être stockée en sécurité en prod)
    private final SecretKey secretKey = Keys.hmacShaKeyFor("01ca69cc7457f12adaaaf6e9e94eca1a7dd5f60d85d012eb282fe452b9202c69".getBytes());

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(secretKey) // ✅ moderne
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(CustomUserDetails userDetails, String nom, String email, String role, boolean abonneExpire) {
        return Jwts.builder()
                .setSubject(email) // sujet = email (identifiant principal)
                .claim("nom", nom)
                .claim("role", role)
                .claim("abonneExpire", abonneExpire) // ✅ on embarque l'attribut
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // expire dans 10h
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }



    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public String extractClaim(String token, String claimKey) {
        return extractAllClaims(token).get(claimKey, String.class);
    }

}
