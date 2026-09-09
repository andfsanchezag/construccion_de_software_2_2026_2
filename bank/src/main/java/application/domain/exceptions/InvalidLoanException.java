package application.domain.exceptions;

public class InvalidLoanException extends DomainException {

    public InvalidLoanException(String message) {
        super(message);
    }
}