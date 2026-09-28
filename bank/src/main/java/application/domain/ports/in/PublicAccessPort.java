package application.domain.ports.in;

import application.domain.models.NaturalCustomer;
import application.domain.models.BusinessCustomer;
import application.domain.models.AuthenticationResult;
import application.domain.models.User;

import java.util.List;

public interface PublicAccessPort {

    AuthenticationResult login(User user);

    void logout(User user);

    NaturalCustomer registerNaturalCustomer(NaturalCustomer customer);

    BusinessCustomer registerBusinessCustomer(BusinessCustomer customer);

    User registerCustomerUser(User user);
}