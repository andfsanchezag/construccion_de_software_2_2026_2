package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class OperationResponseDTO {

    private String operationId;
    private String operationType;
    private LocalDateTime executionDate;
    private String performedBy;
    private String userRole;
    private String affectedProductId;
    private Map<String, Object> details;
}