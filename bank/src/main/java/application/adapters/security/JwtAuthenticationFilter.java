package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.models.User;
import application.domain.valueobjects.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            Integer tokenVersion = jwtProvider.extractTokenVersion(token);

            try {
                // Academic design: the User (role, status, customer, PII) is
                // rebuilt entirely from the token claims, with no database
                // round trip. See JwtProvider for the accepted trade-offs.
                User current = jwtProvider.reconstructUser(token);
                AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(current);

                // Reject inactive/blocked users (per the token's own status
                // claim) and obsolete token versions.
                if (tokenVersion != null && isUsable(current, tokenVersion)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
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
