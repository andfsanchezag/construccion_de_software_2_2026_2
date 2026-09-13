package application.domain.exceptions;

public class UnauthorizedCustomerOperationException extends UnauthorizedOperationException {

    public UnauthorizedCustomerOperationException(String message) {
        super(message);
    }
}
