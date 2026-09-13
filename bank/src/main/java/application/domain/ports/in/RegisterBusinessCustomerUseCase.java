package application.domain.ports.in;

import application.domain.models.BusinessCustomer;
import application.domain.models.User;

public interface RegisterBusinessCustomerUseCase {

    BusinessCustomer registerBusinessCustomer(User user, BusinessCustomer customer);
}
