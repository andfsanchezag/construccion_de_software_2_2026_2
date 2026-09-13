package application.domain.services.customer;

import application.domain.exceptions.CustomerAlreadyExistsException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.InvalidLegalRepresentativeException;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RegisterBusinessCustomerUseCase;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Creates and persists a new BusinessCustomer.
 *
 * <p>The legal representative is a {@code NaturalCustomer} domain relationship:
 * the service never receives a primitive legal-representative identifier. The
 * service validates the supplied Domain Model, verifies that the legal
 * representative exists in persistence and satisfies the required state
 * (ACTIVE adult natural customer) through the CustomerRepositoryPort, checks
 * business identification uniqueness, authorizes the actor, executes the Domain
 * behavior ({@code register()}) and registers Operation/AuditLog only after a
 * successful persistence (customer-services.md 8).
 */
@Service
@RequiredArgsConstructor
public class RegisterBusinessCustomerService implements RegisterBusinessCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerRegistrationService authorizeCustomerRegistrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public BusinessCustomer registerBusinessCustomer(User user, BusinessCustomer customer) {
        validateInput(customer);
        validateLegalRepresentativePresent(customer);
        NaturalCustomer persistedRepresentative = requireValidPersistedRepresentative(customer.getLegalRepresentative());
        validateIdentificationUniqueness(customer);
        authorizeCustomerRegistrationService.execute(user, customer);
        customer.setLegalRepresentative(persistedRepresentative);
        customer.register();
        BusinessCustomer saved = (BusinessCustomer) customerRepositoryPort.save(customer);
        registerBusinessCustomerRegistration(user, saved);
        return saved;
    }

    private void validateInput(BusinessCustomer customer) {
        if (customer == null) {
            throw new InvalidCustomerException("Business customer must be provided.");
        }
        if (customer.getIdentification() == null || customer.getIdentification().isBlank()) {
            throw new InvalidCustomerException("Business customer identification must be provided.");
        }
    }

    private void validateLegalRepresentativePresent(BusinessCustomer customer) {
        if (!customer.hasLegalRepresentative()) {
            throw new InvalidLegalRepresentativeException(
                    "A legal representative is required for business customer registration.");
        }
    }

    private NaturalCustomer requireValidPersistedRepresentative(NaturalCustomer legalRepresentative) {
        Optional<Customer> persistedOpt = customerRepositoryPort.findByIdentification(legalRepresentative);
        if (persistedOpt.isEmpty()) {
            throw new InvalidLegalRepresentativeException(
                    "Legal representative with identification " + legalRepresentative.getIdentification()
                            + " does not exist as a customer.");
        }
        Customer persisted = persistedOpt.get();
        if (!(persisted instanceof NaturalCustomer)) {
            throw new InvalidLegalRepresentativeException("Legal representative must be a natural customer.");
        }
        NaturalCustomer representative = (NaturalCustomer) persisted;
        if (!CustomerStatus.ACTIVE.equals(representative.getStatus())) {
            throw new InvalidLegalRepresentativeException(
                    "Legal representative " + representative.getIdentification()
                            + " must be an ACTIVE customer.");
        }
        if (!representative.isAdult()) {
            throw new InvalidLegalRepresentativeException(
                    "Legal representative " + representative.getIdentification()
                            + " must be at least 18 years old.");
        }
        return representative;
    }

    private void validateIdentificationUniqueness(BusinessCustomer customer) {
        if (customerRepositoryPort.existsByIdentification(customer)) {
            throw new CustomerAlreadyExistsException(customer.getIdentification());
        }
    }

    private void registerBusinessCustomerRegistration(User user, BusinessCustomer customer) {
        Operation operation = new Operation();
        operation.setOperationType(OperationType.CUSTOMER_REGISTRATION);
        operation.setExecutionDate(LocalDateTime.now());
        operation.setPerformedBy(user);
        registerOperationAndAuditService.execute(operation, Map.of(
                "customerType", "BUSINESS_CUSTOMER",
                "identification", customer.getIdentification(),
                "legalRepresentative", customer.getLegalRepresentative().getIdentification(),
                "status", customer.getStatus().getCode()
        ));
    }
}