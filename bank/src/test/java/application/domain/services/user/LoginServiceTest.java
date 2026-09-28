package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.AuthenticationResult;
import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginServiceTest {

    private FakeUsers users;
    private LoginService service;

    @BeforeEach
    void setUp() {
        users = new FakeUsers();
        service = new LoginService(users, new AcceptingPasswords(), new FixedTokens());
    }

    private User credentials(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    private User storedUser() {
        User stored = new User();
        stored.setUserId(7);
        stored.setUsername("user123");
        stored.setPassword("encoded");
        stored.setEmail("user@example.com");
        stored.setRole(SystemRole.NATURAL_CUSTOMER);
        stored.setStatus(UserStatus.ACTIVE);
        stored.setAuthTokenVersion(1);
        users.save(stored);
        return stored;
    }

    @Test
    void loginReturnsStoredUserAndToken() {
        storedUser();

        AuthenticationResult result = service.login(credentials("user123", "raw"));

        assertEquals(7, result.getAuthenticatedUser().getUserId());
        assertEquals("user@example.com", result.getAuthenticatedUser().getEmail());
        assertEquals("TOKEN-7", result.getToken());
    }

    @Test
    void loginFailsForUnknownUser() {
        assertThrows(InvalidCredentialsException.class,
                () -> service.login(credentials("ghost", "raw")));
    }

    @Test
    void loginFailsForWrongPassword() {
        storedUser();
        LoginService rejecting =
                new LoginService(users, new RejectingPasswords(), new FixedTokens());

        assertThrows(InvalidCredentialsException.class,
                () -> rejecting.login(credentials("user123", "wrong")));
    }

    @Test
    void loginFailsForBlockedUser() {
        User stored = storedUser();
        stored.setStatus(UserStatus.BLOCKED);

        assertThrows(DomainException.class,
                () -> service.login(credentials("user123", "raw")));
    }

    static class FakeUsers implements UserRepositoryPort {
        private final Map<String, User> byUsername = new HashMap<>();
        private final Map<Integer, User> byId = new HashMap<>();

        @Override
        public User save(User user) {
            byUsername.put(user.getUsername(), user);
            byId.put(user.getUserId(), user);
            return user;
        }

        @Override
        public Optional<User> findByUsername(User user) {
            return Optional.ofNullable(byUsername.get(user.getUsername()));
        }

        @Override
        public Optional<User> findById(User user) {
            return Optional.ofNullable(byId.get(user.getUserId()));
        }

        @Override
        public boolean existsByUsername(User user) {
            return byUsername.containsKey(user.getUsername());
        }

        @Override
        public void update(User user) {
            byUsername.put(user.getUsername(), user);
            byId.put(user.getUserId(), user);
        }
    }

    static class AcceptingPasswords implements PasswordServicePort {
        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return true;
        }

        @Override
        public String encrypt(String rawPassword) {
            return "encoded";
        }
    }

    static class RejectingPasswords extends AcceptingPasswords {
        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return false;
        }
    }

    static class FixedTokens implements JwtServicePort {
        @Override
        public String generateToken(User user) {
            return "TOKEN-" + user.getUserId();
        }

        @Override
        public boolean validateToken(String token) {
            return true;
        }

        @Override
        public Claims getClaims(String token) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer extractUserId(String token) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer extractTokenVersion(String token) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User reconstructUser(String token) {
            throw new UnsupportedOperationException();
        }
    }
}
