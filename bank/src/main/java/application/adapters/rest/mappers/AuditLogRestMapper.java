package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.AuditLogResponseDTO;
import application.domain.models.AuditLog;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class AuditLogRestMapper {

    public AuditLogResponseDTO toResponseDTO(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setAuditId(auditLog.getAuditId());
        dto.setOperationType(auditLog.getOperationType() != null ? auditLog.getOperationType().getCode() : null);
        dto.setOperationDate(auditLog.getOperationDate());
        dto.setPerformedBy(auditLog.getPerformedBy() != null ? auditLog.getPerformedBy().getUsername() : null);
        dto.setUserRole(auditLog.getUserRole() != null ? auditLog.getUserRole().getCode() : null);
        dto.setAffectedProduct(auditLog.getAffectedProduct() != null ? auditLog.getAffectedProduct().getIdentifier() : null);
        dto.setDetails(auditLog.getDetails());
        return dto;
    }
}