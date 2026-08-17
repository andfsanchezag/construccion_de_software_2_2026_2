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
public class ConsultCustomerService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    public Customer execute(User requestingUser, Customer customer) {
        authorizeCustomerOperationService.execute(requestingUser, customer);
        Optional<Customer> found = customerRepositoryPort.findByIdentification(customer);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Customer");
        }
        return found.get();
    }
}
