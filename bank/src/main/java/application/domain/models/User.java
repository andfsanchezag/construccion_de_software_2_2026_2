package application.domain.models;

import application.domain.exceptions.InvalidUserStatusException;
import application.domain.valueobjects.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User extends Person {
    private Integer userId;
    private String username;
    private String password;
    private UserStatus status;
    private Customer customer;
    /**
     * Token version bound to issued JWTs ({@code ver} claim).
     *
     * <p>Every issued token carries the version observed at login time. Any
     * security-relevant change (password change, status transition) bumps this
     * counter, rendering previously issued tokens obsolete: the authentication
     * filter rejects tokens whose {@code ver} no longer matches.
     */
    private Integer authTokenVersion = 1;

    /**
     * Changes the UserStatus enforcing the transition matrix owned by the User
     * Domain Model.
     *
     * <p>Supported transitions:
     * {@code ACTIVE -> BLOCKED}, {@code ACTIVE -> INACTIVE},
     * {@code BLOCKED -> ACTIVE}, {@code INACTIVE -> ACTIVE}.
     *
     * <p>UserStatus is independent from CustomerStatus: this operation never
     * changes the status of the associated customer (Domain Model.md - Customer
     * and User Status).
     */
    public void changeStatus(UserStatus target) {
        if (target == null) {
            throw new InvalidUserStatusException("Target user status must be provided.");
        }
        if (!isValidTransition(status, target)) {
            throw new InvalidUserStatusException(
                    "Invalid user status transition from " + statusCode() + " to " + target.getCode() + ".");
        }
        this.status = target;
        bumpAuthTokenVersion();
    }

    /**
     * Invalidates previously issued tokens by advancing the token version.
     *
     * <p>Must be invoked on every security-relevant mutation (password change,
     * status transition) so that tokens issued before the change are rejected.
     */
    public void bumpAuthTokenVersion() {
        this.authTokenVersion = (this.authTokenVersion == null ? 1 : this.authTokenVersion + 1);
    }

    /**
     * The user status transition matrix (user-authentication-services.md -
     * Change User Status / Status Validation).
     */
    private static boolean isValidTransition(UserStatus current, UserStatus target) {
        return (UserStatus.ACTIVE.equals(current) && UserStatus.BLOCKED.equals(target))
                || (UserStatus.ACTIVE.equals(current) && UserStatus.INACTIVE.equals(target))
                || (UserStatus.BLOCKED.equals(current) && UserStatus.ACTIVE.equals(target))
                || (UserStatus.INACTIVE.equals(current) && UserStatus.ACTIVE.equals(target));
    }

    private String statusCode() {
        return status == null ? "UNKNOWN" : status.getCode();
    }
}
