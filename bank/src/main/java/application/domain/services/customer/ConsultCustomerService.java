package application.domain.services.customer;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.in.ConsultCustomerUseCase;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Retrieves a customer and returns its Domain Model (NaturalCustomer or
 * BusinessCustomer) through the common Customer abstraction.
 *
 * <p>Read-only use case (customer-services.md 9): validate the domain
 * representation, retrieve the persisted customer, validate existence and
 * authorize the actor. No Operation or AuditLog is generated because no business
 * state is mutated.
 */
@Service
@RequiredArgsConstructor
public class ConsultCustomerService implements ConsultCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    @Override
    public Customer consultCustomer(User user, Customer customer) {
        validateInput(customer);
        Customer persisted = requireExistingCustomer(customer);
        authorizeCustomerOperationService.execute(user, persisted);
        return persisted;
    }

    private void validateInput(Customer customer) {
        if (customer == null) {
            throw new InvalidCustomerException("Customer must be provided.");
        }
        if (customer.getIdentification() == null || customer.getIdentification().isBlank()) {
            throw new InvalidCustomerException("Customer identification must be provided.");
        }
    }

    private Customer requireExistingCustomer(Customer customer) {
        Optional<Customer> found = customerRepositoryPort.findByIdentification(customer);
        if (found.isEmpty()) {
            throw new CustomerNotFoundException(
                    "Customer with identification " + customer.getIdentification() + " was not found.");
        }
        return found.get();
    }
}
