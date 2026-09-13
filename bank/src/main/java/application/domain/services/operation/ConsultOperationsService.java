package application.domain.services.operation;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankingProduct;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consults business Operations by user or by affected product.
 *
 * <p>Read-only use case: the requesting actor must be operationally ACTIVE; no
 * Operation or AuditLog is generated (operation-audit-services.md - Consult
 * Operations).
 */
@Service
@RequiredArgsConstructor
public class ConsultOperationsService {

    private final OperationRepositoryPort operationRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public List<Operation> executeByUser(User requestingUser, User user) {
        validateRequestingUser(requestingUser);
        return operationRepositoryPort.findByUser(user);
    }

    public List<Operation> executeByProduct(User requestingUser, BankingProduct product) {
        validateRequestingUser(requestingUser);
        return operationRepositoryPort.findByProduct(product);
    }

    private void validateRequestingUser(User requestingUser) {
        if (requestingUser == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(requestingUser);
    }
}
