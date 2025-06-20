package com.esiitech.monbondocteurv2.securite;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.service.UtilisateurService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@AllArgsConstructor
@Service
public class JwtService {
    private final String ENCRYPTION_KEY = "01ca69cc7457f12adaaaf6e9e94eca1a7dd5f60d85d012eb282fe452b9202c69";
    @Autowired
    private UtilisateurService utilisateurService;


    public Map<String, String> generate(String username){
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);
        return this.generateJwt(utilisateur);
    }

    public String extractUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate =this.getClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }


    private <T> T getClaim(String token, Function<Claims, T> function ) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // Cette méthode génère un JWT à partir des informations de l'utilisateur
    private Map<String, String> generateJwt(Utilisateur utilisateur) {
        // Définit l'heure actuelle et l'expiration du JWT
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 1 * 60 * 60 * 1000;  // 5 Heures

        // Crée les informations de l'utilisateur à inclure dans le JWT
        final  Map<String, Object> claims = Map.of(
                "id", utilisateur.getId(),               // <--- ajoute cette ligne
                "nom", utilisateur.getNom(),
                "email", utilisateur.getEmail(),
                "role", utilisateur.getRole(),
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, utilisateur.getEmail()
        );



        // Génére le JWT avec les informations et la clé de signature
        final String bearer = Jwts.builder()
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .setSubject(utilisateur.getEmail())
                .setClaims(claims)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        // Retourne le JWT dans un format de map avec la clé "bearer"
        return Map.of("token", bearer);
    }

    // Cette méthode retourne la clé de signature pour le JWT
    private Key getKey() {
        // Décode la clé d'encryption en base64
        final byte[] decode = Decoders.BASE64.decode(ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(decode);  // Retourne la clé pour signer le JWT
    }


}
