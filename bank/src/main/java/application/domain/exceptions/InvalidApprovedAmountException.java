package application.domain.exceptions;

public class InvalidApprovedAmountException extends DomainException {

    public InvalidApprovedAmountException(String message) {
        super(message);
    }
}