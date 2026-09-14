package application.adapters.rest.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApproveLoanRequestDTO {

    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
}