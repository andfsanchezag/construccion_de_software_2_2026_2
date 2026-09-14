package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import application.domain.valueobjects.SystemRole;
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
import java.util.HashMap;
import java.util.Map;
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
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole() != null ? user.getRole().getCode() : null);
        
        if (user.getPerson() != null) {
            claims.put("identification", user.getPerson().getIdentification());
        }
        
        if (user.getCustomer() != null) {
            claims.put("customerId", user.getCustomer().getIdentification());
            claims.put("customerType", user.getCustomer() instanceof application.domain.models.BusinessCustomer ? "BUSINESS" : "NATURAL");
        }

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
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
    public User reconstructUser(String token) {
        Claims claims = getClaims(token);
        
        User user = new User();
        user.setUserId(claims.get("userId", String.class));
        user.setUsername(claims.getSubject());
        user.setEmail(claims.get("email", String.class));
        
        String roleCode = claims.get("role", String.class);
        if (roleCode != null) {
            user.setRole(SystemRole.fromCode(roleCode));
        }
        
        String identification = claims.get("identification", String.class);
        if (identification != null) {
            application.domain.models.Person person = new application.domain.models.Person();
            person.setIdentification(identification);
            user.setPerson(person);
        }
        
        String customerId = claims.get("customerId", String.class);
        if (customerId != null) {
            application.domain.models.Customer customer = new application.domain.models.NaturalCustomer();
            customer.setIdentification(customerId);
            user.setCustomer(customer);
        }
        
        return user;
    }
}