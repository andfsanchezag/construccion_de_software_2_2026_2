package application.adapters.rest.dtos.requests;

import lombok.Data;

@Data
public class RegisterUserRequestDTO {

    private String customerIdentification;
    private String username;
    private String password;
    private String role;
}