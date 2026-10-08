package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequestDTO {

    @NotBlank
    @Size(max = 60)
    private String customerIdentification;

    @NotBlank
    @Size(max = 80)
    private String username;

    @NotBlank
    @Size(max = 200)
    private String password;

    @Size(max = 40)
    @NotBlank
    @Pattern(regexp = "NATURAL_CUSTOMER|BUSINESS_CUSTOMER|BUSINESS_OPERATOR|BUSINESS_SUPERVISOR")
    private String role;
}