package application.domain.exceptions;

public class InvalidDepositException extends DomainException {

    public InvalidDepositException(String message) {
        super(message);
    }
}
