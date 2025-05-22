package ar.edu.utn.frc.tup.piii.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.security.Key;

@Component
public class JwtUtils {
    private final Key SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    String secretString = Base64.getEncoder().encodeToString(SECRET.getEncoded());
//    private final SecretKey SECRET = Keys.hmacShaKeyFor("miClaveUltraSecretaSegura123456".getBytes());
    private final long TIME_EXPIRATION = 3600_000;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TIME_EXPIRATION))
                .signWith(SECRET, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername (String token){
        Jws<Claims> parsedToken = Jwts.parserBuilder()
                .setSigningKey(SECRET) // clave con la que se firmó el token
                .build()
                .parseClaimsJws(token); // <-- acá lo parseás
        return parsedToken.getBody().getSubject();
    }
}
