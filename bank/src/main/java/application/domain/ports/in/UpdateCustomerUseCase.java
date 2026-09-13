package application.domain.ports.in;

import application.domain.models.Customer;
import application.domain.models.User;

public interface UpdateCustomerUseCase {

    Customer updateCustomer(User user, Customer customer);
}
