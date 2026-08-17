package application.domain.services.customer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCustomerService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    public Customer execute(User requestingUser, Customer customer) {
        authorizeCustomerOperationService.execute(requestingUser, customer);
        customerRepositoryPort.findByIdentification(customer)
                .orElseThrow(() -> new EntityNotFoundException("Customer"));
        validateIdentificationUniqueness(customer);
        customerRepositoryPort.update(customer);
        return customer;
    }

    private void validateIdentificationUniqueness(Customer customer) {
        customerRepositoryPort.findByIdentification(customer).ifPresent(existing -> {
            if (!existing.getIdentification().equals(customer.getIdentification())) {
                throw new DomainException(
                        "Identification " + customer.getIdentification() + " is already in use.");
            }
        });
    }
}
