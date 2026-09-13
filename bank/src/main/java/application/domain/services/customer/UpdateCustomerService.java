package application.domain.services.customer;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.models.Customer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.UpdateCustomerUseCase;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Updates the mutable information belonging to an existing customer.
 *
 * <p>Follows the canonical update pattern (customer-services.md 10): validate the
 * desired state Domain Model, load the authoritative persisted customer, validate
 * existence, authorize the actor, apply the allowed changes through the Customer
 * Domain behavior ({@code updateFrom}) and only then persist and register the
 * Operation/AuditLog. Status is never changed here; it has its own dedicated use
 * case. Identification is an identity attribute and is not mutated by a generic
 * update.
 */
@Service
@RequiredArgsConstructor
public class UpdateCustomerService implements UpdateCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Customer updateCustomer(User user, Customer customer) {
        validateInput(customer);
        Customer existing = requireExistingCustomer(customer);
        authorizeCustomerOperationService.execute(user, existing);
        existing.updateFrom(customer);
        customerRepositoryPort.update(existing);
        registerCustomerUpdate(user, existing);
        return existing;
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

    private void registerCustomerUpdate(User user, Customer customer) {
        Operation operation = new Operation();
        operation.setOperationType(OperationType.CUSTOMER_UPDATE);
        operation.setExecutionDate(LocalDateTime.now());
        operation.setPerformedBy(user);
        registerOperationAndAuditService.execute(operation, Map.of(
                "identification", customer.getIdentification(),
                "name", customer.getName(),
                "email", customer.getEmail(),
                "phoneNumber", customer.getPhoneNumber(),
                "address", customer.getAddress()
        ));
    }
}
