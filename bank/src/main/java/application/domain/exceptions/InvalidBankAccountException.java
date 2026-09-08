package application.domain.exceptions;

public class InvalidBankAccountException extends DomainException {

    public InvalidBankAccountException(String message) {
        super(message);
    }
}
