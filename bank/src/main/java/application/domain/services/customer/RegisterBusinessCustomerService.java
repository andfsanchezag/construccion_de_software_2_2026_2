package application.domain.services.customer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.valueobjects.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegisterBusinessCustomerService {

    private final CustomerRepositoryPort customerRepositoryPort;

    public BusinessCustomer execute(BusinessCustomer customer) {
        validateIdentificationUniqueness(customer);
        validateLegalRepresentative(customer);
        customer.setStatus(CustomerStatus.ACTIVE);
        return (BusinessCustomer) customerRepositoryPort.save(customer);
    }

    private void validateIdentificationUniqueness(BusinessCustomer customer) {
        if (customerRepositoryPort.existsByIdentification(customer)) {
            throw new DomainException(
                    "A customer with identification " + customer.getIdentification() + " already exists.");
        }
    }

    private void validateLegalRepresentative(BusinessCustomer customer) {
        if (customer.getLegalRepresentative() == null) {
            throw new DomainException("A legal representative is required for business customer registration.");
        }
        Optional<Customer> repOpt = customerRepositoryPort.findByIdentification(customer.getLegalRepresentative());
        if (repOpt.isEmpty()) {
            throw new EntityNotFoundException("Legal representative");
        }
    }
}