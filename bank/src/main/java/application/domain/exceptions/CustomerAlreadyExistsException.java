package application.domain.exceptions;

public class CustomerAlreadyExistsException extends DomainException {

    public CustomerAlreadyExistsException(String identification) {
        super("A customer with identification " + identification + " already exists.");
    }
}
