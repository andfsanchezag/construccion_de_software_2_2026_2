package application.domain.exceptions;

public class DestinationAccountNotActiveException extends DomainException {

    public DestinationAccountNotActiveException(String message) {
        super(message);
    }
}