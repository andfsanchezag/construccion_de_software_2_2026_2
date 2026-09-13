package application.domain.services.customer;

import application.domain.exceptions.CustomerAlreadyExistsException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.models.NaturalCustomer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RegisterNaturalCustomerUseCase;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Creates and persists a new NaturalCustomer.
 *
 * <p>Follows the canonical Customer service pattern (customer-services.md 3):
 * validate the input Domain Model, validate Domain invariants (age), check
 * identification uniqueness through the CustomerRepositoryPort, authorize the
 * actor, execute the Customer Domain behavior ({@code register()}), persist and
 * only then register the Operation and AuditLog. The caller cannot control the
 * initial CustomerStatus; the Domain Model establishes it.
 */
@Service
@RequiredArgsConstructor
public class RegisterNaturalCustomerService implements RegisterNaturalCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerRegistrationService authorizeCustomerRegistrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public NaturalCustomer registerNaturalCustomer(User user, NaturalCustomer customer) {
        validateInput(customer);
        validateIdentificationUniqueness(customer);
        authorizeCustomerRegistrationService.execute(user, customer);
        customer.register();
        NaturalCustomer saved = (NaturalCustomer) customerRepositoryPort.save(customer);
        registerCustomerRegistration(user, saved);
        return saved;
    }

    private void validateInput(NaturalCustomer customer) {
        if (customer == null) {
            throw new InvalidCustomerException("Natural customer must be provided.");
        }
        if (customer.getIdentification() == null || customer.getIdentification().isBlank()) {
            throw new InvalidCustomerException("Customer identification must be provided.");
        }
    }

    private void validateIdentificationUniqueness(NaturalCustomer customer) {
        if (customerRepositoryPort.existsByIdentification(customer)) {
            throw new CustomerAlreadyExistsException(customer.getIdentification());
        }
    }

    private void registerCustomerRegistration(User user, NaturalCustomer customer) {
        Operation operation = new Operation();
        operation.setOperationType(OperationType.CUSTOMER_REGISTRATION);
        operation.setExecutionDate(LocalDateTime.now());
        operation.setPerformedBy(user);
        registerOperationAndAuditService.execute(operation, Map.of(
                "customerType", "NATURAL_CUSTOMER",
                "identification", customer.getIdentification(),
                "status", customer.getStatus().getCode()
        ));
    }
}
