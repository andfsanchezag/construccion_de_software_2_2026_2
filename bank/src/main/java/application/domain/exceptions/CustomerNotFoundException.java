package application.domain.exceptions;

public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
