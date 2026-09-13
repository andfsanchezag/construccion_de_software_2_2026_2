package application.domain.exceptions;

public class InvalidOperationException extends DomainException {

    public InvalidOperationException(String message) {
        super(message);
    }
}