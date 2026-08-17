package application.domain.ports.out;

import application.domain.models.User;

public interface PasswordServicePort {

    boolean matches(User user);

    String encrypt(User user);
}
