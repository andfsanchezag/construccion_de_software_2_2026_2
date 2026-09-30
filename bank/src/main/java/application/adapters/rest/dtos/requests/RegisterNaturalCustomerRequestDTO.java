package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterNaturalCustomerRequestDTO {

    @Size(max = 60)
    private String identification;

    @Size(max = 120)
    private String name;

    @Size(max = 120)
    private String email;

    @Size(max = 40)
    private String phoneNumber;

    @Size(max = 200)
    private String address;
    private LocalDate birthDate;
}