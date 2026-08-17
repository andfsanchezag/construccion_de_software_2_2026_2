package application.domain.services.customer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCustomerService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    public Customer execute(User requestingUser, Customer customer) {
        authorizeCustomerOperationService.execute(requestingUser, customer);
        Optional<Customer> existing = customerRepositoryPort.findByIdentification(customer);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Customer");
        }
        customerRepositoryPort.update(customer);
        return customer;
    }
}
