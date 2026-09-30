package application.adapters.rest.dtos.requests;

import lombok.Data;

import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

@Data
public class ApproveLoanRequestDTO {

    @Digits(integer = 17, fraction = 2)
    private BigDecimal approvedAmount;

    @Digits(integer = 5, fraction = 4)
    private BigDecimal interestRate;
}