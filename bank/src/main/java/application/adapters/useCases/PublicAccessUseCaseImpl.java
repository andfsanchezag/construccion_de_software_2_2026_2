package application.adapters.useCases;

import application.domain.models.NaturalCustomer;
import application.domain.models.BusinessCustomer;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.services.customer.RegisterNaturalCustomerService;
import application.domain.services.customer.RegisterBusinessCustomerService;
import application.domain.services.user.LoginService;
import application.domain.services.user.RegisterCustomerUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final LoginService loginService;
    private final RegisterNaturalCustomerService registerNaturalCustomerService;
    private final RegisterBusinessCustomerService registerBusinessCustomerService;
    private final RegisterCustomerUserService registerCustomerUserService;

    @Override
    public User login(User user) {
        return loginService.login(user);
    }

    @Override
    public void logout(User user) {
        // Logout is handled client-side by discarding the JWT token
        // Server-side logout would require token blacklisting which is not implemented
    }

    @Override
    public NaturalCustomer registerNaturalCustomer(NaturalCustomer customer) {
        return registerNaturalCustomerService.registerNaturalCustomer(null, customer);
    }

    @Override
    public BusinessCustomer registerBusinessCustomer(BusinessCustomer customer) {
        return registerBusinessCustomerService.registerBusinessCustomer(null, customer);
    }

    @Override
    public User registerCustomerUser(User user) {
        return registerCustomerUserService.registerCustomerUser(null, user);
    }
}