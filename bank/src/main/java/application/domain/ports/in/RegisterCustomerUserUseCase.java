package application.domain.ports.in;

import application.domain.models.User;

public interface RegisterCustomerUserUseCase {

    User registerCustomerUser(User requestingUser, User user);
}
