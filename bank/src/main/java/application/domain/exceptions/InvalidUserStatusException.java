package application.domain.exceptions;

public class InvalidUserStatusException extends DomainException {

    public InvalidUserStatusException(String message) {
        super(message);
    }
}
