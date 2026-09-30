package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequestDTO {

    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
    private String reference;
}