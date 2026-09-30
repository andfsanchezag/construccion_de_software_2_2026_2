package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterBusinessCustomerRequestDTO {

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

    @Size(max = 60)
    private String legalRepresentativeIdentification;
}