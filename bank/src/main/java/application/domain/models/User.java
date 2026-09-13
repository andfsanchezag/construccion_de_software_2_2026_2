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
