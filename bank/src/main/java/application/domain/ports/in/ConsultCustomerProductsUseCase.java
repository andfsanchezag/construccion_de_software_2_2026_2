package application.domain.ports.in;

import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.User;

public interface ConsultCustomerProductsUseCase {

    CustomerProducts consultCustomerProducts(User user, Customer customer);
}
