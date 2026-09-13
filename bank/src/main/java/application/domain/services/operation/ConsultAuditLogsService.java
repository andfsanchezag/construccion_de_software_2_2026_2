package application.domain.services.operation;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.AuditLog;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consults AuditLog records by user or by affected product.
 *
 * <p>Read-only use case: the requesting actor must be operationally ACTIVE; no
 * Operation or AuditLog is generated (operation-audit-services.md - Consult
 * Audit Logs / Consult Product Audit Trail / Consult User Audit Trail).
 */
@Service
@RequiredArgsConstructor
public class ConsultAuditLogsService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public List<AuditLog> executeByUser(User requestingUser, User user) {
        validateRequestingUser(requestingUser);
        return auditLogRepositoryPort.findByUser(user);
    }

    public List<AuditLog> executeByProduct(User requestingUser, BankingProduct product) {
        validateRequestingUser(requestingUser);
        return auditLogRepositoryPort.findByProduct(product);
    }

    private void validateRequestingUser(User requestingUser) {
        if (requestingUser == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(requestingUser);
    }
}
