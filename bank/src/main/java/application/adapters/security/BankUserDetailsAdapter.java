package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.exceptions.DomainException;
import application.domain.models.User;
import application.domain.services.user.LoadAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adaptador de seguridad (Spring {@link UserDetailsService}) hacia el dominio.
 *
 * <p>Responsabilidades del adaptador, no del dominio: parsear el
 * {@code subject} (String del claim {@code sub}) a id tipado, delegar la
 * carga autoritativa al {@link LoadAuthenticatedUserService}, envolver el
 * {@link User} en el DTO {@link AuthenticatedUserPrincipal} y traducir
 * excepciones de dominio a {@link UsernameNotFoundException}.
 */
@Service
@RequiredArgsConstructor
public class BankUserDetailsAdapter implements UserDetailsService {

    private final LoadAuthenticatedUserService loadAuthenticatedUserService;

    @Override
    public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
        Integer userId = parseSubject(subject);
        if (userId == null) {
            throw new UsernameNotFoundException("Invalid token subject.");
        }
        try {
            User stored = loadAuthenticatedUserService.loadById(userId);
            return new AuthenticatedUserPrincipal(stored);
        } catch (DomainException e) {
            throw new UsernameNotFoundException("User not found.", e);
        }
    }

    private Integer parseSubject(String subject) {
        try {
            return subject == null ? null : Integer.valueOf(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
