package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultAuditLogsService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public List<AuditLog> executeByUser(User requestingUser, User user) {
        return auditLogRepositoryPort.findByUser(user);
    }

    public List<AuditLog> executeByProduct(User requestingUser, BankingProduct product) {
        return auditLogRepositoryPort.findByProduct(product);
    }
}
