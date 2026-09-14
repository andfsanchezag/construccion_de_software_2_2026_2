package application.adapters.rest.dtos.requests;

import lombok.Data;

@Data
public class RegisterBusinessCustomerRequestDTO {

    private String identification;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String legalRepresentativeIdentification;
}