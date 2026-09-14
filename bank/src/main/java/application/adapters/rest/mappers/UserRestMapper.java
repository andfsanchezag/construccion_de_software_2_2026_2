package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.domain.models.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserRestMapper {

    public User toDomain(LoginRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        return user;
    }

    public LoginResponseDTO toResponseDTO(String token, User user, Long expiresIn) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setExpiresIn(expiresIn);
        
        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole() != null ? user.getRole().getCode() : null);
        
        response.setUser(userInfo);
        return response;
    }
}