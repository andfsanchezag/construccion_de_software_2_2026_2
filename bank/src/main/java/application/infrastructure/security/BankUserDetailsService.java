package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads the current user by its internal immutable id ({@code sub} claim)
 * through the {@link UserRepositoryPort}.
 *
 * <p>Rejects missing users with {@link UsernameNotFoundException}. Status and
 * token-version enforcement happen in the authentication filter and through
 * the {@link AuthenticatedUserPrincipal} account flags.
 */
@Service
@RequiredArgsConstructor
public class BankUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
        Integer userId = parseSubject(subject);
        if (userId == null) {
            throw new UsernameNotFoundException("Invalid token subject.");
        }
        User probe = new User();
        probe.setUserId(userId);
        Optional<User> stored = userRepositoryPort.findById(probe);
        if (stored.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        return new AuthenticatedUserPrincipal(stored.get());
    }

    private Integer parseSubject(String subject) {
        try {
            return subject == null ? null : Integer.valueOf(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
