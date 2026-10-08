package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterEmployeeUserRequestDTO {

    @Size(max = 80)
    @NotBlank
    private String username;

    @Size(max = 200)
    @NotBlank
    private String password;

    @Size(max = 40)
    @NotBlank
    @Pattern(regexp = "NATURAL_CUSTOMER|BUSINESS_CUSTOMER|TELLER_EMPLOYEE|COMMERCIAL_EMPLOYEE|BUSINESS_OPERATOR|BUSINESS_SUPERVISOR|INTERNAL_ANALYST")
    private String role;

    @Size(max = 120)
    @NotBlank
    private String email;

    @Size(max = 60)
    @NotBlank
    private String identification;

    @Size(max = 120)
    @NotBlank
    private String name;

    @Size(max = 40)
    @NotBlank
    private String phoneNumber;

    @Size(max = 200)
    @NotBlank
    private String address;
}