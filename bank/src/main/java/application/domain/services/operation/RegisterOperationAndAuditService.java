package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.models.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Coordinates the registration of the Operation and the corresponding AuditLog
 * produced by a state-changing business operation.
 *
 * This service is the single traceability registration point used by the Bank Account
 * services. The persistence adapters implementing the OperationRepositoryPort and
 * AuditLogRepositoryPort must coordinate a common consistency mechanism (for example a
 * shared transaction or an outbox pattern) so that the product state change, the
 * Operation, and the AuditLog are persisted consistently, as required by the Bank
 * Account services specification (section 17 - Transactional Consistency).
 *
 * The domain guarantees the registration order and propagates any failure; it never
 * accesses the database directly.
 */
@Service
@RequiredArgsConstructor
public class RegisterOperationAndAuditService {

    private final RegisterOperationService registerOperationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Operation execute(Operation operation, Map<String, Object> auditDetails) {
        Operation persistedOperation = registerOperationService.execute(operation);

        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(operation.getOperationType());
        auditLog.setOperationDate(LocalDateTime.now());
        auditLog.setPerformedBy(operation.getPerformedBy());
        auditLog.setUserRole(operation.getPerformedBy().getRole());
        auditLog.setAffectedProduct(operation.getAffectedProduct());
        auditLog.setDetails(auditDetails);

        registerAuditLogService.execute(auditLog);
        return persistedOperation;
    }
}
