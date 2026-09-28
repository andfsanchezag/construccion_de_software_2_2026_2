package application.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtProviderTest {

    private JwtProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtProvider();
        ReflectionTestUtils.setField(provider, "secret",
                "9a6f8b12c34d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f");
        ReflectionTestUtils.setField(provider, "expirationMs", 3600000L);
    }

    private User user() {
        User user = new User();
        user.setUserId(42);
        user.setUsername("user123");
        user.setEmail("user@example.com");
        user.setIdentification("1017123456");
        user.setRole(SystemRole.NATURAL_CUSTOMER);
        user.setAuthTokenVersion(3);
        return user;
    }

    @Test
    void tokenCarriesOnlyIdentityClaims() {
        String token = provider.generateToken(user());

        Claims claims = provider.getClaims(token);
        assertEquals("42", claims.getSubject());
        assertEquals(3, ((Number) claims.get("ver")).intValue());
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        assertNull(claims.get("username"));
        assertNull(claims.get("email"));
        assertNull(claims.get("role"));
        assertNull(claims.get("identification"));
        assertNull(claims.get("customerId"));
        assertNull(claims.get("userId"));
    }

    @Test
    void tokenIdsAreUniquePerIssuance() {
        String first = provider.generateToken(user());
        String second = provider.generateToken(user());

        assertTrue(provider.validateToken(first));
        assertTrue(provider.validateToken(second));
        assertFalse(provider.getClaims(first).getId().equals(provider.getClaims(second).getId()));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = provider.generateToken(user());

        assertFalse(provider.validateToken(token + "tampered"));
    }

    @Test
    void extractorsRoundTrip() {
        String token = provider.generateToken(user());

        assertEquals(42, provider.extractUserId(token));
        assertEquals(3, provider.extractTokenVersion(token));
    }

    @Test
    void issuanceRequiresInternalId() {
        User withoutId = new User();

        assertThrows(IllegalArgumentException.class, () -> provider.generateToken(withoutId));
        assertThrows(IllegalArgumentException.class, () -> provider.generateToken(null));
    }

    @Test
    void reconstructUserCarriesOnlyIdentityReference() {
        String token = provider.generateToken(user());

        User reference = provider.reconstructUser(token);

        assertEquals(42, reference.getUserId());
        assertEquals(3, reference.getAuthTokenVersion());
        assertNull(reference.getUsername());
        assertNull(reference.getEmail());
    }
}
