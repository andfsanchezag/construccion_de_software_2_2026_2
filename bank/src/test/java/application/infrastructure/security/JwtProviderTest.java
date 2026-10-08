package application.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.models.BusinessCustomer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import io.jsonwebtoken.Claims;
import java.time.LocalDate;
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
        user.setName("User Test");
        user.setPhoneNumber("3000000000");
        user.setAddress("Street 1");
        user.setRole(SystemRole.NATURAL_CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthTokenVersion(3);
        return user;
    }

    private NaturalCustomer naturalCustomer() {
        NaturalCustomer customer = new NaturalCustomer();
        customer.setIdentification("1017123456");
        customer.setName("User Test");
        customer.setEmail("user@example.com");
        customer.setPhoneNumber("3000000000");
        customer.setAddress("Street 1");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        return customer;
    }

    private BusinessCustomer businessCustomer() {
        BusinessCustomer customer = new BusinessCustomer();
        customer.setIdentification("9001234567");
        customer.setName("Acme Corp");
        customer.setEmail("contact@acme.com");
        customer.setPhoneNumber("3000000001");
        customer.setAddress("Avenue 2");
        customer.setStatus(CustomerStatus.ACTIVE);
        NaturalCustomer legalRepresentative = new NaturalCustomer();
        legalRepresentative.setIdentification("1017999999");
        customer.setLegalRepresentative(legalRepresentative);
        return customer;
    }

    @Test
    void tokenCarriesFullUserClaims() {
        String token = provider.generateToken(user());

        Claims claims = provider.getClaims(token);
        assertEquals("42", claims.getSubject());
        assertEquals(3, ((Number) claims.get("ver")).intValue());
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        assertEquals("user123", claims.get("username"));
        assertEquals("user@example.com", claims.get("email"));
        assertEquals(SystemRole.NATURAL_CUSTOMER.getCode(), claims.get("role"));
        assertEquals(UserStatus.ACTIVE.getCode(), claims.get("status"));
        assertEquals("1017123456", claims.get("identification"));
        assertEquals("User Test", claims.get("name"));
        assertEquals("3000000000", claims.get("phoneNumber"));
        assertEquals("Street 1", claims.get("address"));
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
    void reconstructUserCarriesFullUserData() {
        String token = provider.generateToken(user());

        User reconstructed = provider.reconstructUser(token);

        assertEquals(42, reconstructed.getUserId());
        assertEquals(3, reconstructed.getAuthTokenVersion());
        assertEquals("user123", reconstructed.getUsername());
        assertEquals("user@example.com", reconstructed.getEmail());
        assertEquals(SystemRole.NATURAL_CUSTOMER, reconstructed.getRole());
        assertEquals(UserStatus.ACTIVE, reconstructed.getStatus());
        assertEquals("1017123456", reconstructed.getIdentification());
        assertEquals("User Test", reconstructed.getName());
        assertEquals("3000000000", reconstructed.getPhoneNumber());
        assertEquals("Street 1", reconstructed.getAddress());
        assertNull(reconstructed.getCustomer());
    }

    @Test
    void reconstructUserRebuildsNaturalCustomerFromClaims() {
        User user = user();
        user.setCustomer(naturalCustomer());

        User reconstructed = provider.reconstructUser(provider.generateToken(user));

        assertNotNull(reconstructed.getCustomer());
        assertTrue(reconstructed.getCustomer() instanceof NaturalCustomer);
        NaturalCustomer customer = (NaturalCustomer) reconstructed.getCustomer();
        assertEquals("1017123456", customer.getIdentification());
        assertEquals("User Test", customer.getName());
        assertEquals("user@example.com", customer.getEmail());
        assertEquals("3000000000", customer.getPhoneNumber());
        assertEquals("Street 1", customer.getAddress());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertEquals(LocalDate.of(1990, 1, 1), customer.getBirthDate());
    }

    @Test
    void reconstructUserRebuildsBusinessCustomerFromClaims() {
        User user = user();
        user.setRole(SystemRole.BUSINESS_CUSTOMER);
        user.setCustomer(businessCustomer());

        User reconstructed = provider.reconstructUser(provider.generateToken(user));

        assertNotNull(reconstructed.getCustomer());
        assertTrue(reconstructed.getCustomer() instanceof BusinessCustomer);
        BusinessCustomer customer = (BusinessCustomer) reconstructed.getCustomer();
        assertEquals("9001234567", customer.getIdentification());
        assertEquals("Acme Corp", customer.getName());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertNotNull(customer.getLegalRepresentative());
        assertEquals("1017999999", customer.getLegalRepresentative().getIdentification());
    }
}
