package application.domain.ports.in;

import application.domain.models.NaturalCustomer;
import application.domain.models.User;

public interface RegisterNaturalCustomerUseCase {

    NaturalCustomer registerNaturalCustomer(User user, NaturalCustomer customer);
}
