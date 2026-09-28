package application.infrastructure.security;

import application.domain.models.User;
import application.domain.valueobjects.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final BankUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            Integer tokenVersion = jwtProvider.extractTokenVersion(token);
            String subject = subjectOf(token);

            try {
                AuthenticatedUserPrincipal principal =
                        (AuthenticatedUserPrincipal) userDetailsService.loadUserByUsername(subject);
                User current = principal.getUser();

                // Reject inactive/blocked users and obsolete token versions.
                // Roles/permissions always come from the freshly loaded Domain
                // Model, never from JWT claims.
                if (tokenVersion != null && isUsable(current, tokenVersion)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (UsernameNotFoundException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String subjectOf(String token) {
        try {
            return jwtProvider.getClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isUsable(User stored, Integer tokenVersion) {
        if (!UserStatus.ACTIVE.equals(stored.getStatus())) {
            return false;
        }
        Integer currentVersion = stored.getAuthTokenVersion() != null ? stored.getAuthTokenVersion() : 1;
        return currentVersion.equals(tokenVersion);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
