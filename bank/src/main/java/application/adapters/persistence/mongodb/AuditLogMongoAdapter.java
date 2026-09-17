package application.adapters.persistence.mongodb;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.persistence.mongodb.mappers.AuditLogMongoMapper;
import application.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.models.AuditLog;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for the audit trail backed by Spring Data MongoDB
 * (audit_db, audit_logs collection).
 *
 * Implements AuditLogRepositoryPort while keeping the domain free of any MongoDB
 * dependency: the conversion is performed by AuditLogMongoMapper.
 */
@Repository
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private final AuditLogMongoRepository auditLogRepository;

    public AuditLogMongoAdapter(AuditLogMongoRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        AuditLogDocument saved = auditLogRepository.save(AuditLogMongoMapper.toDocument(auditLog));
        return AuditLogMongoMapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        return auditLogRepository.findByPerformedByUserId(user.getUserId()).stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByProduct(BankingProduct product) {
        if (product == null || product.getIdentifier() == null) {
            return List.of();
        }
        return auditLogRepository.findByAffectedProductIdentifier(product.getIdentifier()).stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByOperationType(AuditLog auditLog) {
        if (auditLog == null || auditLog.getOperationType() == null) {
            return List.of();
        }
        return auditLogRepository.findByOperationType(auditLog.getOperationType().getCode()).stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }
}