package application.adapters.persistence.mongodb;

import java.util.ArrayList;
import java.util.List;

import application.domain.models.AuditLog;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;

/**
 * In-memory test double of {@link AuditLogRepositoryPort}.
 *
 * <p>It lives in the test sources so the unit tests of the domain services can run
 * without MongoDB. Production persistence is provided by the MongoDB adapter under
 * {@code application.adapters.persistence.mongodb}.
 */
public final class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

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
