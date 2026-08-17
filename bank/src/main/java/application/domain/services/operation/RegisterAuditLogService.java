package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public AuditLog execute(AuditLog auditLog) {
        return auditLogRepositoryPort.save(auditLog);
    }
}
