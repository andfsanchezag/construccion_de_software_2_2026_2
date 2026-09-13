package application.domain.exceptions;

public class InvalidUserException extends DomainException {

    public InvalidUserException(String message) {
        super(message);
    }
}