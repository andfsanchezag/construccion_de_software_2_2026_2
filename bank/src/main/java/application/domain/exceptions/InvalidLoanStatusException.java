package application.domain.exceptions;

public class InvalidLoanStatusException extends DomainException {

    public InvalidLoanStatusException(String message) {
        super(message);
    }
}