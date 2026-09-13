package application.domain.services.operation;

import application.domain.exceptions.InvalidAuditLogException;
import application.domain.models.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Registers an AuditLog record through the AuditLogRepositoryPort.
 *
 * <p>Audit records are immutable and append-only; the service validates the
 * required Domain Model state before persistence (operation-audit-services.md -
 * Register Audit Log / Domain Validations / Immutability).
 */
@Service
@RequiredArgsConstructor
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public AuditLog execute(AuditLog auditLog) {
        validateAuditLog(auditLog);
        return auditLogRepositoryPort.save(auditLog);
    }

    private void validateAuditLog(AuditLog auditLog) {
        if (auditLog == null) {
            throw new InvalidAuditLogException("Audit log must be provided.");
        }
        if (auditLog.getOperationType() == null) {
            throw new InvalidAuditLogException("Audit log operation type must be provided.");
        }
    }
}