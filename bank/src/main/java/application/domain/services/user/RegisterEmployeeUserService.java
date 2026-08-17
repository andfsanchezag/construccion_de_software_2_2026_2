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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterEmployeeUserService {

    private static final Set<SystemRole> EMPLOYEE_ROLES = Set.of(
            SystemRole.TELLER_EMPLOYEE,
            SystemRole.COMMERCIAL_EMPLOYEE,
            SystemRole.BUSINESS_OPERATOR,
            SystemRole.BUSINESS_SUPERVISOR,
            SystemRole.INTERNAL_ANALYST
    );

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;

    public User execute(User requestingUser, User employee) {
        validateInternalAnalystAuthorizationService.execute(requestingUser);
        validateEmployeeRole(employee);
        validateUsernameUniqueness(employee);
        String securePassword = passwordServicePort.encrypt(employee);
        employee.setPassword(securePassword);
        employee.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(employee);
    }

    private void validateEmployeeRole(User employee) {
        if (!EMPLOYEE_ROLES.contains(employee.getRole())) {
            throw new DomainException("Role " + employee.getRole().getCode() + " is not a valid employee role.");
        }
    }

    private void validateUsernameUniqueness(User employee) {
        if (userRepositoryPort.existsByUsername(employee)) {
            throw new DomainException("Username " + employee.getUsername() + " is already in use.");
        }
    }
}
