package application.domain.exceptions;

public class InvalidCustomerStatusException extends DomainException {

    public InvalidCustomerStatusException(String message) {
        super(message);
    }
}
