package application.domain.exceptions;

public class InvalidWithdrawalException extends DomainException {

    public InvalidWithdrawalException(String message) {
        super(message);
    }
}
