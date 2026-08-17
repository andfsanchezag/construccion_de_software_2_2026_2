package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.models.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterOperationAndAuditService {

    private final RegisterOperationService registerOperationService;
    private final RegisterAuditLogService registerAuditLogService;

    public void execute(Operation operation, Map<String, Object> auditDetails) {
        registerOperationService.execute(operation);

        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(operation.getOperationType());
        auditLog.setOperationDate(LocalDateTime.now());
        auditLog.setPerformedBy(operation.getPerformedBy());
        auditLog.setUserRole(operation.getPerformedBy().getRole());
        auditLog.setAffectedProduct(operation.getAffectedProduct());
        auditLog.setDetails(auditDetails);

        registerAuditLogService.execute(auditLog);
    }
}
