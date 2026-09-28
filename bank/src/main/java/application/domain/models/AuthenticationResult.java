package application.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outcome of a successful authentication.
 *
 * <p>Carries the persisted authenticated {@link User} Domain Model together
 * with the freshly issued identity token. The token itself embeds no PII and
 * no permission snapshots; it only references this user ({@code sub}) and its
 * token version ({@code ver}).
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthenticationResult {
    private User authenticatedUser;
    private String token;

    public AuthenticationResult(User authenticatedUser, String token) {
        this.authenticatedUser = authenticatedUser;
        this.token = token;
    }
}
