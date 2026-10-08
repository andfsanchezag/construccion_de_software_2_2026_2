package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransferRequestDTO {

    @NotBlank
    @Size(max = 60)
    private String sourceAccountNumber;

    @NotBlank
    @Size(max = 60)
    private String destinationAccountNumber;

    @NotNull
    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
    private String description;
}