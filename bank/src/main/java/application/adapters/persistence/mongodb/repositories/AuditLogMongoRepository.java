package application.adapters.persistence.mongodb.repositories;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the AuditLog persistence document.
 */
public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByPerformedByUserId(Integer performedByUserId);

    List<AuditLogDocument> findByAffectedProductIdentifier(String affectedProductIdentifier);

    List<AuditLogDocument> findByOperationType(String operationType);
}
