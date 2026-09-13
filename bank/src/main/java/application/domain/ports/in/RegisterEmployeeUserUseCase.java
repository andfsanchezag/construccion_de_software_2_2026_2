package application.domain.ports.in;

import application.domain.models.User;

public interface RegisterEmployeeUserUseCase {

    User registerEmployeeUser(User requestingUser, User employee);
}
