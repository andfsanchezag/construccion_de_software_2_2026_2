package application.domain.exceptions;

public class InvalidLoanAmountException extends DomainException {

    public InvalidLoanAmountException(String message) {
        super(message);
    }
}