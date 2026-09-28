package application.domain.ports.in;

import application.domain.models.AuthenticationResult;
import application.domain.models.User;

public interface LoginUseCase {

    AuthenticationResult login(User user);
}
