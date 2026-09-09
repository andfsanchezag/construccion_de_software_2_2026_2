package application.domain.exceptions;

public class InvalidLoanTypeException extends DomainException {

    public InvalidLoanTypeException(String message) {
        super(message);
    }
}