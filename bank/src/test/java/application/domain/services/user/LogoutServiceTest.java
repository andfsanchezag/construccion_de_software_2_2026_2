package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogoutServiceTest {

    private FakeUsers users;
    private LogoutService service;

    @BeforeEach
    void setUp() {
        users = new FakeUsers();
        service = new LogoutService(users);
    }

    private User actor(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    @Test
    void logoutBumpsTokenVersion() {
        User stored = new User();
        stored.setUserId(9);
        stored.setStatus(UserStatus.ACTIVE);
        stored.setAuthTokenVersion(4);
        users.save(stored);

        service.logout(actor(9));

        assertEquals(5, users.findById(actor(9)).orElseThrow().getAuthTokenVersion());
    }

    @Test
    void logoutFailsWithoutAuthenticatedUser() {
        assertThrows(DomainException.class, () -> service.logout(null));
        assertThrows(DomainException.class, () -> service.logout(new User()));
    }

    @Test
    void logoutFailsForUnknownUser() {
        assertThrows(EntityNotFoundException.class, () -> service.logout(actor(404)));
    }

    static class FakeUsers implements UserRepositoryPort {
        private final Map<Integer, User> byId = new HashMap<>();

        @Override
        public User save(User user) {
            byId.put(user.getUserId(), user);
            return user;
        }

        @Override
        public Optional<User> findByUsername(User user) {
            return byId.values().stream()
                    .filter(candidate -> candidate.getUsername() != null
                            && candidate.getUsername().equals(user.getUsername()))
                    .findFirst();
        }

        @Override
        public Optional<User> findById(User user) {
            return Optional.ofNullable(byId.get(user == null ? null : user.getUserId()));
        }

        @Override
        public boolean existsByUsername(User user) {
            return findByUsername(user).isPresent();
        }

        @Override
        public void update(User user) {
            byId.put(user.getUserId(), user);
        }
    }
}
