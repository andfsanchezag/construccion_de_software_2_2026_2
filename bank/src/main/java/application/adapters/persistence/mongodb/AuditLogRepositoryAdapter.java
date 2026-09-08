package application.adapters.persistence.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import application.domain.models.AuditLog;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;

/**
 * In-memory persistence adapter for AuditLog.
 *
 * It stays inside the adapter layer and implements the AuditLogRepositoryPort,
 * so the domain never depends on MongoDB. This is the MongoDB output adapter
 * placeholder; keep the domain contract unchanged when replacing the store.
 */
@Service
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final List<AuditLog> store = new ArrayList<>();

    @Override
    public AuditLog save(AuditLog auditLog) {
        if (auditLog.getAuditId() == null) {
            auditLog.setAuditId("AUD-" + (store.size() + 1));
        }
        store.add(auditLog);
        return auditLog;
    }

    @Override
    public List<AuditLog> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        return store.stream()
                .filter(log -> log.getPerformedBy() != null
                        && user.getUserId().equals(log.getPerformedBy().getUserId()))
                .toList();
    }

    @Override
    public List<AuditLog> findByProduct(BankingProduct product) {
        if (product == null || product.getIdentifier() == null) {
            return List.of();
        }
        return store.stream()
                .filter(log -> log.getAffectedProduct() != null
                        && product.getIdentifier().equals(log.getAffectedProduct().getIdentifier()))
                .toList();
    }

    @Override
    public List<AuditLog> findByOperationType(AuditLog auditLog) {
        if (auditLog == null || auditLog.getOperationType() == null) {
            return List.of();
        }
        return store.stream()
                .filter(log -> auditLog.getOperationType().equals(log.getOperationType()))
                .toList();
    }
}
