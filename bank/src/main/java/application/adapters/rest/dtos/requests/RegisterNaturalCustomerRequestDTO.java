package application.adapters.rest.dtos.requests;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterNaturalCustomerRequestDTO {

    private String identification;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate birthDate;
}