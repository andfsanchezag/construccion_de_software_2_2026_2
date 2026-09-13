package application.domain.exceptions;

public class InvalidAuditLogException extends DomainException {

    public InvalidAuditLogException(String message) {
        super(message);
    }
}