package application.domain.services.customer;

import application.domain.models.NaturalCustomer;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.valueobjects.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class RegisterNaturalCustomerService {

    private final CustomerRepositoryPort customerRepositoryPort;

    public NaturalCustomer execute(NaturalCustomer customer) {
        validateAge(customer);
        validateIdentificationUniqueness(customer);
        customer.setStatus(CustomerStatus.ACTIVE);
        return (NaturalCustomer) customerRepositoryPort.save(customer);
    }

    private void validateAge(NaturalCustomer customer) {
        int age = Period.between(customer.getBirthDate(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new application.domain.exceptions.DomainException(
                    "Customer must be at least 18 years old.");
        }
    }

    private void validateIdentificationUniqueness(NaturalCustomer customer) {
        if (customerRepositoryPort.existsByIdentification(customer)) {
            throw new application.domain.exceptions.DomainException(
                    "A customer with identification " + customer.getIdentification() + " already exists.");
        }
    }
}
