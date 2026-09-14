package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuditLogResponseDTO {

    private String auditId;
    private String operationType;
    private LocalDateTime operationDate;
    private String performedBy;
    private String userRole;
    private String affectedProduct;
    private Map<String, Object> details;
}