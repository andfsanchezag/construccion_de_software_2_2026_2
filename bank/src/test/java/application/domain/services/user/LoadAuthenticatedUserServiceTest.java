package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoadAuthenticatedUserServiceTest {

    private FakeUsers users;
    private LoadAuthenticatedUserService service;

    @BeforeEach
    void setUp() {
        users = new FakeUsers();
        service = new LoadAuthenticatedUserService(users);
    }

    @Test
    void loadsAuthoritativeUserById() {
        User stored = new User();
        stored.setUserId(11);
        stored.setUsername("teller1");
        users.save(stored);

        assertEquals(11, service.loadById(11).getUserId());
        assertEquals("teller1", service.loadById(11).getUsername());
    }

    @Test
    void rejectsUnknownUserWithDomainException() {
        assertThrows(EntityNotFoundException.class, () -> service.loadById(999));
    }

    @Test
    void rejectsNullIdWithDomainException() {
        assertThrows(DomainException.class, () -> service.loadById(null));
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
            return Optional.empty();
        }

        @Override
        public Optional<User> findById(User user) {
            return Optional.ofNullable(byId.get(user == null ? null : user.getUserId()));
        }

        @Override
        public boolean existsByUsername(User user) {
            return false;
        }

        @Override
        public void update(User user) {
            byId.put(user.getUserId(), user);
        }
    }
}
