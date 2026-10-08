package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterCompanyUserRequestDTO {

    @NotBlank
    @Size(max = 80)
    private String username;

    @NotBlank
    @Size(max = 200)
    private String password;

    @Size(max = 40)
    @NotBlank
    @Pattern(regexp = "BUSINESS_CUSTOMER|BUSINESS_OPERATOR|BUSINESS_SUPERVISOR")
    private String role;

    @Size(max = 120)
    private String email;

    @Size(max = 60)
    private String identification;

    @Size(max = 120)
    private String name;
}