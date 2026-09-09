package application.domain.exceptions;

public class InvalidLoanStatusTransitionException extends DomainException {

    public InvalidLoanStatusTransitionException(String from, String to) {
        super("Invalid loan status transition from " + from + " to " + to + ".");
    }
}