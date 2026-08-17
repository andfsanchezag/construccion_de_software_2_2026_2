package application.domain.models;

import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditLog {
    private String auditId;
    private OperationType operationType;
    private LocalDateTime operationDate;
    private User performedBy;
    private SystemRole userRole;
    private BankingProduct affectedProduct;
    private Map<String, Object> details;
}
