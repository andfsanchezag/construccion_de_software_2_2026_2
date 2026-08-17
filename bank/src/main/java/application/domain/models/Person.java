package application.domain.models;

import application.domain.valueobjects.SystemRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Person {
    private String identification;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private SystemRole role;
}
