package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider implements JwtServicePort {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User with an internal id must be provided to issue a token.");
        }
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        // Identity-only token: sub (internal immutable id), jti, ver, iat, exp.
        // No PII and no permission/role snapshots.
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .id(UUID.randomUUID().toString())
                .claim("ver", user.getAuthTokenVersion() != null ? user.getAuthTokenVersion() : 1)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public Integer extractUserId(String token) {
        try {
            String subject = getClaims(token).getSubject();
            return subject == null ? null : Integer.valueOf(subject);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Integer extractTokenVersion(String token) {
        try {
            Object ver = getClaims(token).get("ver");
            if (ver instanceof Number number) {
                return number.intValue();
            }
            if (ver instanceof String text) {
                return Integer.valueOf(text);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public User reconstructUser(String token) {
        User reference = new User();
        reference.setUserId(extractUserId(token));
        reference.setAuthTokenVersion(extractTokenVersion(token));
        return reference;
    }
}
