package application.domain.services.customer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.valueobjects.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeCustomerStatusService {

    private final CustomerRepositoryPort customerRepositoryPort;

    public Customer execute(User requestingUser, Customer customer, CustomerStatus newStatus) {
        Customer persisted = customerRepositoryPort.findByIdentification(customer)
                .orElseThrow(() -> new EntityNotFoundException("Customer"));
        validateTransition(persisted.getStatus(), newStatus);
        persisted.setStatus(newStatus);
        customerRepositoryPort.update(persisted);
        return persisted;
    }

    private void validateTransition(CustomerStatus current, CustomerStatus next) {
        boolean valid = (current.equals(CustomerStatus.ACTIVE) && next.equals(CustomerStatus.INACTIVE))
                || (current.equals(CustomerStatus.ACTIVE) && next.equals(CustomerStatus.BLOCKED))
                || (current.equals(CustomerStatus.BLOCKED) && next.equals(CustomerStatus.ACTIVE))
                || (current.equals(CustomerStatus.INACTIVE) && next.equals(CustomerStatus.ACTIVE));
        if (!valid) {
            throw new InvalidStatusTransitionException(current.getCode(), next.getCode());
        }
    }
}
