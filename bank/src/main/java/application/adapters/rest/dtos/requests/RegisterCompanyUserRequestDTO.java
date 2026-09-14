package application.adapters.rest.dtos.requests;

import lombok.Data;

@Data
public class RegisterCompanyUserRequestDTO {

    private String username;
    private String password;
    private String role;
    private String email;
    private String identification;
    private String name;
}