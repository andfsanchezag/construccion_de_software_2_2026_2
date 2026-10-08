package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequestDTO {

    @NotNull
    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;

    @Size(max = 60)
    private String clientIdentification;

    @Pattern(regexp = "COP|USD|EUR")
    private String currency;
}