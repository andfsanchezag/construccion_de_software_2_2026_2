package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.models.User;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateInternalAnalystAuthorizationService;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterEmployeeUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;

    public User execute(User requestingUser, User employee) {
        validateInternalAnalystAuthorizationService.execute(requestingUser);
        validateEmployeeRole(employee);
        validateUsernameUniqueness(employee);
        String securePassword = passwordServicePort.encrypt(employee.getPassword());
        employee.setPassword(securePassword);
        employee.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(employee);
    }

    private void validateEmployeeRole(User employee) {
        SystemRole role = employee.getRole();
        if (!SystemRole.TELLER_EMPLOYEE.equals(role)
                && !SystemRole.COMMERCIAL_EMPLOYEE.equals(role)
                && !SystemRole.BUSINESS_OPERATOR.equals(role)
                && !SystemRole.BUSINESS_SUPERVISOR.equals(role)
                && !SystemRole.INTERNAL_ANALYST.equals(role)) {
            throw new DomainException("Role " + role.getCode() + " is not a valid employee role.");
        }
    }

    private void validateUsernameUniqueness(User employee) {
        if (userRepositoryPort.existsByUsername(employee)) {
            throw new DomainException("Username " + employee.getUsername() + " is already in use.");
        }
    }
}
