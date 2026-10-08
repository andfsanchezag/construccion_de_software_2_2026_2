package application.infrastructure.security;

import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Academic design choice: this project embeds the full User (and associated
 * Customer, when present) snapshot as JWT claims so that every request can be
 * authenticated/authorized from the token alone, without a database round
 * trip ({@link #reconstructUser(String)}). This trade-off is intentional for
 * this academic project and accepts that role/status changes only take
 * effect for a user the next time it logs in (a new token is issued); the
 * {@code ver} claim plus {@code User.authTokenVersion} still allow explicitly
 * revoking previously issued tokens on security-relevant changes.
 */
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

        // sub (internal immutable id), jti, iat, exp plus the full set of
        // User/Customer claims so the request can be authenticated and
        // authorized without reloading the Domain Model from the database.
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry));
        buildUserClaims(user).forEach(builder::claim);
        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * Builds the full set of custom claims describing the User (and its
     * associated Customer, when present) so {@link #reconstructUser(String)}
     * can rebuild the Domain Model entirely from the token.
     *
     * <p>Null values are intentionally included: {@link JwtBuilder#claim}
     * treats a {@code null} value as "do not set/remove this claim", so
     * absent attributes are simply omitted from the signed token.
     */
    private Map<String, Object> buildUserClaims(User user) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("ver", user.getAuthTokenVersion() != null ? user.getAuthTokenVersion() : 1);
        claims.put("username", user.getUsername());
        claims.put("status", user.getStatus() != null ? user.getStatus().getCode() : null);
        claims.put("role", user.getRole() != null ? user.getRole().getCode() : null);
        claims.put("identification", user.getIdentification());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("address", user.getAddress());

        Customer customer = user.getCustomer();
        if (customer != null) {
            claims.put("custType", customer instanceof BusinessCustomer ? "BUSINESS" : "NATURAL");
            claims.put("custIdentification", customer.getIdentification());
            claims.put("custName", customer.getName());
            claims.put("custEmail", customer.getEmail());
            claims.put("custPhoneNumber", customer.getPhoneNumber());
            claims.put("custAddress", customer.getAddress());
            claims.put("custStatus", customer.getStatus() != null ? customer.getStatus().getCode() : null);
            if (customer instanceof NaturalCustomer naturalCustomer) {
                claims.put("custBirthDate",
                        naturalCustomer.getBirthDate() != null ? naturalCustomer.getBirthDate().toString() : null);
            } else if (customer instanceof BusinessCustomer businessCustomer) {
                claims.put("custLegalRepIdentification",
                        businessCustomer.getLegalRepresentative() != null
                                ? businessCustomer.getLegalRepresentative().getIdentification()
                                : null);
            }
        }
        return claims;
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

    /**
     * Rebuilds the full User (and associated Customer, when present) from the
     * token claims alone — no database access.
     *
     * <p>Academic trade-off accepted for this project: the rebuilt snapshot is
     * only as fresh as the token's issuance time, so role/status changes are
     * not reflected until the user logs in again. {@link #extractTokenVersion}
     * combined with {@code User.authTokenVersion} is how the filter still
     * rejects tokens issued before a security-relevant change.
     */
    @Override
    public User reconstructUser(String token) {
        Claims claims = getClaims(token);
        User user = new User();
        user.setUserId(extractUserId(token));
        user.setAuthTokenVersion(extractTokenVersion(token));
        user.setUsername(claims.get("username", String.class));
        user.setStatus(parseUserStatus(claims.get("status", String.class)));
        user.setRole(SystemRole.fromCode(claims.get("role", String.class)));
        user.setIdentification(claims.get("identification", String.class));
        user.setName(claims.get("name", String.class));
        user.setEmail(claims.get("email", String.class));
        user.setPhoneNumber(claims.get("phoneNumber", String.class));
        user.setAddress(claims.get("address", String.class));

        String custType = claims.get("custType", String.class);
        if (custType != null) {
            Customer customer = "BUSINESS".equals(custType) ? new BusinessCustomer() : new NaturalCustomer();
            customer.setIdentification(claims.get("custIdentification", String.class));
            customer.setName(claims.get("custName", String.class));
            customer.setEmail(claims.get("custEmail", String.class));
            customer.setPhoneNumber(claims.get("custPhoneNumber", String.class));
            customer.setAddress(claims.get("custAddress", String.class));
            customer.setStatus(parseCustomerStatus(claims.get("custStatus", String.class)));
            if (customer instanceof NaturalCustomer naturalCustomer) {
                String birthDate = claims.get("custBirthDate", String.class);
                if (birthDate != null) {
                    naturalCustomer.setBirthDate(LocalDate.parse(birthDate));
                }
            } else if (customer instanceof BusinessCustomer businessCustomer) {
                String legalRepIdentification = claims.get("custLegalRepIdentification", String.class);
                if (legalRepIdentification != null) {
                    NaturalCustomer legalRepresentative = new NaturalCustomer();
                    legalRepresentative.setIdentification(legalRepIdentification);
                    businessCustomer.setLegalRepresentative(legalRepresentative);
                }
            }
            user.setCustomer(customer);
        }
        return user;
    }

    private UserStatus parseUserStatus(String code) {
        if (code == null) {
            return null;
        }
        if (UserStatus.ACTIVE.getCode().equals(code)) {
            return UserStatus.ACTIVE;
        }
        if (UserStatus.INACTIVE.getCode().equals(code)) {
            return UserStatus.INACTIVE;
        }
        if (UserStatus.BLOCKED.getCode().equals(code)) {
            return UserStatus.BLOCKED;
        }
        return null;
    }

    private CustomerStatus parseCustomerStatus(String code) {
        if (code == null) {
            return null;
        }
        if (CustomerStatus.ACTIVE.getCode().equals(code)) {
            return CustomerStatus.ACTIVE;
        }
        if (CustomerStatus.INACTIVE.getCode().equals(code)) {
            return CustomerStatus.INACTIVE;
        }
        if (CustomerStatus.BLOCKED.getCode().equals(code)) {
            return CustomerStatus.BLOCKED;
        }
        return null;
    }
}
