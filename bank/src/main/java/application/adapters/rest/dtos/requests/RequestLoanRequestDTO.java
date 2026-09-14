package application.adapters.rest.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestLoanRequestDTO {

    private String loanType;
    private BigDecimal requestedAmount;
    private Integer termInMonths;
    private String destinationAccountNumber;
}