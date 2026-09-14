package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponseDTO {

    private String transferId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private String approvedBy;
    private LocalDateTime approvalDate;
}