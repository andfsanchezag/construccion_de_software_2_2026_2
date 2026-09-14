package application.adapters.rest.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanPaymentRequestDTO {

    private String sourceAccountNumber;
    private BigDecimal amount;
}