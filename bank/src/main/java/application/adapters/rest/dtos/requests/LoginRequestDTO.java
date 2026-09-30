package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @Size(max = 80)
    private String username;

    @Size(max = 200)
    private String password;
}