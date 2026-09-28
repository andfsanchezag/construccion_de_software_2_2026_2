package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.domain.models.Operation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OperationRestMapper {

    public OperationResponseDTO toResponseDTO(Operation operation) {
        if (operation == null) {
            return null;
        }
        
        OperationResponseDTO dto = new OperationResponseDTO();
        dto.setOperationId(operation.getOperationId() != null ? String.valueOf(operation.getOperationId()) : null);
        dto.setOperationType(operation.getOperationType() != null ? operation.getOperationType().getCode() : null);
        dto.setExecutionDate(operation.getExecutionDate());
        dto.setPerformedBy(operation.getPerformedBy() != null ? operation.getPerformedBy().getUsername() : null);
        dto.setUserRole(operation.getPerformedBy() != null && operation.getPerformedBy().getRole() != null 
                ? operation.getPerformedBy().getRole().getCode() : null);
        
        // For affected product, we need to get the identifier
        if (operation.getAffectedProduct() != null) {
            if (operation.getAffectedProduct() instanceof application.domain.models.BankAccount) {
                dto.setAffectedProductId(((application.domain.models.BankAccount) operation.getAffectedProduct()).getIdentifier());
            } else if (operation.getAffectedProduct() instanceof application.domain.models.Loan) {
                dto.setAffectedProductId(((application.domain.models.Loan) operation.getAffectedProduct()).getIdentifier());
            } else if (operation.getAffectedProduct() instanceof application.domain.models.Transfer) {
                dto.setAffectedProductId(((application.domain.models.Transfer) operation.getAffectedProduct()).getIdentifier());
            }
        }
        
        // Operation domain model carries no free-form details; response details stay null.
        dto.setDetails(null);
        return dto;
    }
}