package application.domain.models;

import application.domain.valueobjects.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User extends Person {
    private Integer userId;
    private String username;
    private String password;
    private UserStatus status;
    private Customer customer;
}
