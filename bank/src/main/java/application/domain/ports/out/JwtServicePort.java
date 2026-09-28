package application.domain.ports.out;

import application.domain.models.User;
import io.jsonwebtoken.Claims;

public interface JwtServicePort {

    /**
     * Issues a token carrying only the identity reference:
     * {@code sub} (internal immutable user id), {@code jti}, {@code ver}
     * (User.authTokenVersion), {@code iat} and {@code exp}.
     *
     * <p>No PII (username, email, identification) and no permission/role
     * snapshots are embedded: authorization is always resolved from the current
     * domain model on each request.
     */
    String generateToken(User user);
    boolean validateToken(String token);
    Claims getClaims(String token);

    /** Internal user id ({@code sub} claim); {@code null} if absent/unparseable. */
    Integer extractUserId(String token);

    /** Token version ({@code ver} claim); {@code null} if absent/unparseable. */
    Integer extractTokenVersion(String token);

    /**
     * Minimal token reference carrying only the identity ({@code sub}) and
     * version ({@code ver}). Kept for compatibility; request authentication
     * must load the full user via UserRepositoryPort instead of trusting
     * token content.
     */
    User reconstructUser(String token);
}
