package application.adapters.rest.dtos.requests;

import lombok.Data;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class ApproveLoanRequestDTO {

    @NotNull
    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal approvedAmount;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 5, fraction = 4)
    private BigDecimal interestRate;
}