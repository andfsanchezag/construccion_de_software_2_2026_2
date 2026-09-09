package application.domain.exceptions;

public class InvalidLoanTermException extends DomainException {

    public InvalidLoanTermException(String message) {
        super(message);
    }
}