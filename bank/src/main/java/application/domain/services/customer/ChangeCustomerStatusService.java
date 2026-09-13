package application.domain.services.customer;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.InvalidCustomerStatusException;
import application.domain.models.Customer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.ChangeCustomerStatusUseCase;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeChangeCustomerStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Changes the CustomerStatus of an existing customer.
 *
 * <p>The requested target status is carried by the supplied Customer Domain Model
 * ({@code Customer.status}); the service never receives a primitive identifier or
 * an isolated status attribute (customer-services.md 11.3). The authoritative
 * persisted state is loaded first, the transition is validated and executed by the
 * Customer Domain behavior ({@code changeStatus}), and Operation/AuditLog are
 * registered only after a successful mutation. CustomerStatus and UserStatus
 * remain independent: this use case never touches any User status.
 */
@Service
@RequiredArgsConstructor
public class ChangeCustomerStatusService implements ChangeCustomerStatusUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeChangeCustomerStatusService authorizeChangeCustomerStatusService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Customer changeCustomerStatus(User user, Customer customer) {
        validateInput(customer);
        Customer persisted = requireExistingCustomer(customer);
        authorizeChangeCustomerStatusService.execute(user);
        String previousStatus = statusCodeOf(persisted);
        persisted.changeStatus(customer.getStatus());
        customerRepositoryPort.update(persisted);
        registerCustomerStatusChange(user, persisted, previousStatus);
        return persisted;
    }

    private void validateInput(Customer customer) {
        if (customer == null) {
            throw new InvalidCustomerException("Customer must be provided.");
        }
        if (customer.getIdentification() == null || customer.getIdentification().isBlank()) {
            throw new InvalidCustomerException("Customer identification must be provided.");
        }
        if (customer.getStatus() == null) {
            throw new InvalidCustomerStatusException("Target customer status must be provided.");
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

    private String statusCodeOf(Customer customer) {
        return customer.getStatus() == null ? "UNKNOWN" : customer.getStatus().getCode();
    }

    private void registerCustomerStatusChange(User user, Customer customer, String previousStatus) {
        Operation operation = new Operation();
        operation.setOperationType(OperationType.CUSTOMER_STATUS_CHANGE);
        operation.setExecutionDate(LocalDateTime.now());
        operation.setPerformedBy(user);
        registerOperationAndAuditService.execute(operation, Map.of(
                "identification", customer.getIdentification(),
                "previousStatus", previousStatus,
                "newStatus", customer.getStatus().getCode()
        ));
    }
}
