package application.domain.services.customer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultCustomerService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    public Customer execute(User requestingUser, Customer customer) {
        authorizeCustomerOperationService.execute(requestingUser, customer);
        return customerRepositoryPort.findByIdentification(customer)
                .orElseThrow(() -> new EntityNotFoundException("Customer"));
    }
}
