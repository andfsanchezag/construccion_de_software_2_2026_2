package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequestDTO {

    @Size(max = 60)
    private String customerIdentification;

    @Size(max = 80)
    private String username;

    @Size(max = 200)
    private String password;

    @Size(max = 40)
    @NotBlank
    @Pattern(regexp = "NATURAL_CUSTOMER|BUSINESS_CUSTOMER|TELLER_EMPLOYEE|COMMERCIAL_EMPLOYEE|BUSINESS_OPERATOR|BUSINESS_SUPERVISOR|INTERNAL_ANALYST")
    private String role;
}