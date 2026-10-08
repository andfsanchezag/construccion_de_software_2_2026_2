package application.domain.exceptions;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entity) {
        super(entity + " not found. Verifique que el identificador exista en base de datos "
                + "y que pertenezca al usuario autenticado (ver token JWT).");
    }

    public EntityNotFoundException(String entity, String identifier) {
        super(entity + " not found"
                + (identifier == null || identifier.isBlank() ? "." : " with identifier '" + identifier + "'.")
                + " Verifique que el identificador exista en base de datos "
                + "y que pertenezca al usuario autenticado (ver token JWT).");
    }
}
