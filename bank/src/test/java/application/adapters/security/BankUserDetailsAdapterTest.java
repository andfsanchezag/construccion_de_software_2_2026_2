package application.adapters.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.user.LoadAuthenticatedUserService;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class BankUserDetailsAdapterTest {

    private FakeUsers users;
    private BankUserDetailsAdapter adapter;

    @BeforeEach
    void setUp() {
        users = new FakeUsers();
        adapter = new BankUserDetailsAdapter(new LoadAuthenticatedUserService(users));
    }

    @Test
    void loadsPrincipalFromAuthoritativeModel() {
        User stored = new User();
        stored.setUserId(11);
        stored.setUsername("teller1");
        stored.setRole(SystemRole.TELLER_EMPLOYEE);
        stored.setStatus(UserStatus.ACTIVE);
        stored.setAuthTokenVersion(2);
        users.save(stored);

        AuthenticatedUserPrincipal principal =
                (AuthenticatedUserPrincipal) adapter.loadUserByUsername("11");

        assertEquals(11, principal.getUser().getUserId());
        assertEquals("teller1", principal.getUsername());
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TELLER_EMPLOYEE")));
        assertTrue(principal.isEnabled());
    }

    @Test
    void rejectsUnknownUsers() {
        assertThrows(UsernameNotFoundException.class,
                () -> adapter.loadUserByUsername("999"));
    }

    @Test
    void rejectsInvalidSubjects() {
        assertThrows(UsernameNotFoundException.class,
                () -> adapter.loadUserByUsername("not-a-subject"));
        assertThrows(UsernameNotFoundException.class,
                () -> adapter.loadUserByUsername(null));
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
