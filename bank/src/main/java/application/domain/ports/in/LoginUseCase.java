package application.domain.ports.in;

import application.domain.models.User;

public interface LoginUseCase {

    String login(User user);
}
