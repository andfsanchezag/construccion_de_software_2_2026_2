package application.domain.ports.in;

import application.domain.models.User;

public interface ConsultUserUseCase {

    User consultUser(User requestingUser, User user);
}
