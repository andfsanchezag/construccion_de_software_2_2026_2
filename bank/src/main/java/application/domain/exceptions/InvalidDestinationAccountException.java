package application.domain.exceptions;

public class InvalidDestinationAccountException extends DomainException {

    public InvalidDestinationAccountException(String message) {
        super(message);
    }
}