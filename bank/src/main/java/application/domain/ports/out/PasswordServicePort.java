package application.domain.ports.out;

public interface PasswordServicePort {

    boolean matches(String rawPassword, String encodedPassword);

    String encrypt(String rawPassword);
}
