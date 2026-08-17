package application.domain.exceptions;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entity) {
        super(entity + " not found.");
    }
}
