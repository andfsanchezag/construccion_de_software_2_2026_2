package application.domain.exceptions;

public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException() {
        super("Insufficient balance to complete the operation.");
    }
}
