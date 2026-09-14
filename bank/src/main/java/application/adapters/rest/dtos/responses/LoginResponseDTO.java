package application.adapters.rest.dtos.responses;

import lombok.Data;

@Data
public class LoginResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private String userId;
        private String username;
        private String email;
        private String role;
    }
}