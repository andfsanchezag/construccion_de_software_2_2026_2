package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanResponseDTO {

    private String loanId;
    private String loanType;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private String status;
    private Integer termInMonths;
    private String currency;
    private String applicantIdentification;
    private String destinationAccountNumber;
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
}