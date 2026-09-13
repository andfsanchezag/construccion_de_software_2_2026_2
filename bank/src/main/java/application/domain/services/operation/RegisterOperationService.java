package application.domain.services.operation;

import application.domain.exceptions.InvalidOperationException;
import application.domain.models.Operation;
import application.domain.ports.out.OperationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Registers a successful business Operation through the OperationRepositoryPort.
 *
 * <p>Only callers that have already completed their business action reach this
 * registration point; the service validates the required Domain Model state
 * (operation type, performing user) before persistence
 * (operation-audit-services.md - Register Operation / Domain Validations).
 */
@Service
@RequiredArgsConstructor
public class RegisterOperationService {

    private final OperationRepositoryPort operationRepositoryPort;

    public Operation execute(Operation operation) {
        validateOperation(operation);
        return operationRepositoryPort.save(operation);
    }

    private void validateOperation(Operation operation) {
        if (operation == null) {
            throw new InvalidOperationException("Operation must be provided.");
        }
        if (operation.getOperationType() == null) {
            throw new InvalidOperationException("Operation type must be provided.");
        }
        // performedBy may be null for system/process operations (e.g. ExpireTransfer);
        // interactive operations always set the performing User.
    }
}
