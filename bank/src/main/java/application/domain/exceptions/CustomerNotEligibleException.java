package application.domain.exceptions;

public class CustomerNotEligibleException extends DomainException {

    public CustomerNotEligibleException(String message) {
        super(message);
    }
}
