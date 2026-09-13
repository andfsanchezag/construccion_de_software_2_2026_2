package application.domain.exceptions;

public class InvalidCustomerException extends DomainException {

    public InvalidCustomerException(String message) {
        super(message);
    }
}
