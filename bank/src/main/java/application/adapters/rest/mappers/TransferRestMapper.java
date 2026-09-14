package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.CreateTransferRequestDTO;
import application.adapters.rest.dtos.responses.TransferResponseDTO;
import application.domain.models.Transfer;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.valueobjects.TransferStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
public class TransferRestMapper {

    public Transfer toDomain(CreateTransferRequestDTO dto) {
        Transfer transfer = new Transfer();
        
        BankAccount source = new BankAccount();
        source.setIdentifier(dto.getSourceAccountNumber());
        transfer.setSourceAccount(source);
        
        BankAccount destination = new BankAccount();
        destination.setIdentifier(dto.getDestinationAccountNumber());
        transfer.setDestinationAccount(destination);
        
        transfer.setAmount(dto.getAmount());
        return transfer;
    }

    public TransferResponseDTO toResponseDTO(Transfer transfer) {
        if (transfer == null) {
            return null;
        }
        
        TransferResponseDTO dto = new TransferResponseDTO();
        dto.setTransferId(transfer.getIdentifier());
        dto.setSourceAccountNumber(transfer.getSourceAccount() != null ? transfer.getSourceAccount().getIdentifier() : null);
        dto.setDestinationAccountNumber(transfer.getDestinationAccount() != null ? transfer.getDestinationAccount().getIdentifier() : null);
        dto.setAmount(transfer.getAmount());
        dto.setStatus(transfer.getTransferStatus() != null ? transfer.getTransferStatus().getCode() : null);
        dto.setCreatedAt(transfer.getCreationDate());
        dto.setExecutedAt(transfer.getExecutionDate());
        dto.setApprovedBy(transfer.getApprovedBy() != null ? transfer.getApprovedBy().getUsername() : null);
        dto.setApprovalDate(transfer.getApprovalDate());
        return dto;
    }
}