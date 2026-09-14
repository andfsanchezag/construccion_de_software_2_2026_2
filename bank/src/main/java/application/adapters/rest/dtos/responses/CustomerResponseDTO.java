package application.adapters.rest.dtos.responses;

import lombok.Data;

@Data
public class CustomerResponseDTO {

    private String identification;
    private String name;
    private String email;
    private String status;
    private String customerType;
}