package application.domain.ports.in;

import application.domain.models.Customer;
import application.domain.models.User;

public interface ChangeCustomerStatusUseCase {

    Customer changeCustomerStatus(User user, Customer customer);
}
