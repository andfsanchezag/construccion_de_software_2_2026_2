package application.domain.ports.out;

import application.domain.models.User;
import io.jsonwebtoken.Claims;

public interface JwtServicePort {

    /**
     * Issues a token carrying the full User snapshot (and associated Customer,
     * when present) as claims: {@code sub} (internal immutable user id),
     * {@code jti}, {@code ver} (User.authTokenVersion), {@code iat}, {@code exp},
     * plus username/status/role/PII and nested customer claims.
     *
     * <p>Academic design: this allows {@link #reconstructUser(String)} to
     * rebuild the authenticated user entirely from the token, without a
     * database round trip on every request. The accepted trade-off is that
     * role/status changes only take effect the next time the user logs in.
     */
    String generateToken(User user);
    boolean validateToken(String token);
    Claims getClaims(String token);

    /** Internal user id ({@code sub} claim); {@code null} if absent/unparseable. */
    Integer extractUserId(String token);

    /** Token version ({@code ver} claim); {@code null} if absent/unparseable. */
    Integer extractTokenVersion(String token);

    /**
     * Rebuilds the full User (including its associated Customer, when
     * present) entirely from the token's claims, with no database access.
     */
    User reconstructUser(String token);
}
