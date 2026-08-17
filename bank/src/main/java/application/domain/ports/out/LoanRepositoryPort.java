package application.domain.ports.out;

import application.domain.models.Customer;
import application.domain.models.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {

    Loan save(Loan loan);

    Optional<Loan> findByIdentifier(Loan loan);

    List<Loan> findByApplicant(Customer customer);

    List<Loan> findByStatus(Loan loan);

    void update(Loan loan);
}
