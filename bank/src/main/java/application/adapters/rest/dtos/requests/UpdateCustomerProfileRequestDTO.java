package application.adapters.rest.dtos.requests;

import lombok.Data;

@Data
public class UpdateCustomerProfileRequestDTO {

    private String email;
    private String phoneNumber;
    private String address;
}