package application.adapters.rest.dtos.responses;

import lombok.Data;

@Data
public class UserResponseDTO {

    private String userId;
    private String username;
    private String role;
    private String status;
}