package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.domain.models.Operation;
import application.domain.models.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.Map;

@UtilityClass
public class OperationRestMapper {

    public OperationResponseDTO toResponseDTO(Operation operation) {
        if (operation == null) {
            return null;
        }
        
        OperationResponseDTO dto = new OperationResponseDTO();
        dto.setOperationId(operation.getIdentifier());
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
            } else if (operation.getAffectedProduct() instanceof application.domain.models.Customer) {
                dto.setAffectedProductId(((application.domain.models.Customer) operation.getAffectedProduct()).getIdentification());
            }
        }
        
        // The details are already a Map in the domain model
        dto.setDetails(operation.getDetails());
        return dto;
    }
}