package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanPaymentRequestDTO {

    @Size(max = 60)
    private String sourceAccountNumber;

    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
}