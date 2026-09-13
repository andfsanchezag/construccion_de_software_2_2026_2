package application.domain.ports.in;

import application.domain.models.User;

public interface ChangeUserPasswordUseCase {

    void changePassword(User user);
}
