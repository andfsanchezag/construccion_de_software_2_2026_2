package application.adapters.persistence.mongodb.documents;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB persistence document (repository DTO) for the AuditLog aggregate.
 *
 * <p>Stored in the {@code audit_logs} collection of the {@code audit_db} database. It is
 * completely independent from the domain model: the actor and the affected product are
 * denormalized into plain values so the audit trail remains readable even if the
 * referenced aggregates change, and the domain never depends on MongoDB.
 */
@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogDocument {

    @Id
    private String auditId;

    /** OperationType catalog code. */
    private String operationType;

    private LocalDateTime operationDate;

    /** AuditLog.performedBy denormalized reference. */
    private Integer performedByUserId;
    private String performedByUsername;

    /** SystemRole catalog code of the acting user. */
    private String performedByRole;

    /** AuditLog.userRole catalog code. */
    private String userRole;

    /** AuditLog.affectedProduct denormalized reference. */
    private String affectedProductIdentifier;
    private String affectedProductType;

    /** AuditLog.details free-form payload. */
    private Map<String, Object> details;
}
