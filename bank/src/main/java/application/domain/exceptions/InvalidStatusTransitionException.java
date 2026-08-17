package application.domain.exceptions;

public class InvalidStatusTransitionException extends DomainException {

    public InvalidStatusTransitionException(String from, String to) {
        super("Invalid status transition from " + from + " to " + to + ".");
    }
}
