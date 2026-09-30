package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommercialRequestLoanRequestDTO {

    @Size(max = 60)
    private String customerIdentification;

    @Size(max = 40)
    @NotBlank
    @Pattern(regexp = "PERSONAL|MORTGAGE|VEHICLE|BUSINESS")
    private String loanType;

    @Digits(integer = 17, fraction = 2)
    private BigDecimal requestedAmount;

    private Integer termInMonths;

    @Size(max = 60)
    private String destinationAccountNumber;
}