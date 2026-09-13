package application.domain.ports.in;

import application.domain.models.Customer;
import application.domain.models.User;

public interface ConsultCustomerUseCase {

    Customer consultCustomer(User user, Customer customer);
}
