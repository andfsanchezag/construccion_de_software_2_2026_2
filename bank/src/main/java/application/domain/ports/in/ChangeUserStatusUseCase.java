package application.domain.ports.in;

import application.domain.models.User;

public interface ChangeUserStatusUseCase {

    User changeUserStatus(User requestingUser, User user);
}
