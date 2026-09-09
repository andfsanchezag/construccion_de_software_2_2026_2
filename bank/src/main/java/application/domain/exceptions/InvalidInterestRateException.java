package application.domain.exceptions;

public class InvalidInterestRateException extends DomainException {

    public InvalidInterestRateException(String message) {
        super(message);
    }
}