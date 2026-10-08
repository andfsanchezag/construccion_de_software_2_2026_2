package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterNaturalCustomerRequestDTO {

    @Size(max = 60)
    @NotBlank 
    private String identification;

    @Size(max = 120)
    @NotBlank 
    private String name;

    @Size(max = 120)
    @NotBlank
    private String email;

    @Size(max = 40)
    @NotBlank
    private String phoneNumber;

    @Size(max = 200)
    @NotBlank
    private String address;
    private LocalDate birthDate;
}