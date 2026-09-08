package application.domain.exceptions;

public class InvalidAccountStatusException extends DomainException {

    public InvalidAccountStatusException(String message) {
        super(message);
    }
}
