package application.domain.models;

import application.domain.valueobjects.CustomerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Customer extends Person {
    private CustomerStatus status;
}
