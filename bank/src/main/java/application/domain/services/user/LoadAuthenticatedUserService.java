package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de dominio para cargar el usuario autenticado por su id interno
 * inmutable (claim {@code sub} del JWT).
 *
 * <p>Dominio puro: no conoce Spring Security ni {@code UserDetails}. Solo
 * resuelve el {@link User} autoritativo vía {@link UserRepositoryPort} y
 * falla con excepciones de dominio. El adaptador de seguridad
 * ({@code application.adapters.security.BankUserDetailsAdapter}) lo envuelve
 * en el DTO {@code AuthenticatedUserPrincipal} y traduce a
 * {@code UsernameNotFoundException}.
 */
@Service
@RequiredArgsConstructor
public class LoadAuthenticatedUserService {

    private final UserRepositoryPort userRepositoryPort;

    public User loadById(Integer userId) {
        if (userId == null) {
            throw new DomainException("Authenticated user id must be provided.");
        }
        User probe = new User();
        probe.setUserId(userId);
        return userRepositoryPort.findById(probe)
                .orElseThrow(() -> new EntityNotFoundException("User"));
    }
}
