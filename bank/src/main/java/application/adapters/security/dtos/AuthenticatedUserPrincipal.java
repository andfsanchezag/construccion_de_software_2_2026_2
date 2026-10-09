package application.adapters.security.dtos;

import application.domain.models.User;
import application.domain.valueobjects.UserStatus;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security principal wrapping the {@link User} rebuilt from the JWT
 * claims on each request (see
 * {@code application.adapters.security.JwtProvider#reconstructUser(String)}).
 *
 * <p>Spring Security types remain outside {@code domain/}: this adapter is the
 * only place where the domain user meets Spring's authentication model. Role
 * and customer association come entirely from the signed JWT claims (academic
 * design: no per-request database lookup), so they reflect the state at
 * token-issuance time, not necessarily the latest persisted state.
 */
@Getter
@RequiredArgsConstructor
public class AuthenticatedUserPrincipal implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user == null ? null : user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user != null && !UserStatus.BLOCKED.equals(user.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && UserStatus.ACTIVE.equals(user.getStatus());
    }
}
