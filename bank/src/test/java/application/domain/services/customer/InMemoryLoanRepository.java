package application.domain.services.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.ports.out.LoanRepositoryPort;

final class InMemoryLoanRepository implements LoanRepositoryPort {

    private final List<Loan> store = new ArrayList<>();

    void seed(Loan loan) {
        store.add(loan);
    }

    @Override
    public Loan save(Loan loan) {
        store.add(loan);
        return loan;
    }

    @Override
    public Optional<Loan> findByIdentifier(Loan loan) {
        if (loan == null || loan.getIdentifier() == null) {
            return Optional.empty();
        }
        return store.stream()
                .filter(stored -> loan.getIdentifier().equals(stored.getIdentifier()))
                .findFirst();
    }

    @Override
    public List<Loan> findByApplicant(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return List.of();
        }
        return store.stream()
                .filter(loan -> loan.getApplicant() != null
                        && customer.getIdentification().equals(loan.getApplicant().getIdentification()))
                .toList();
    }

    @Override
    public List<Loan> findByStatus(Loan loan) {
        if (loan == null || loan.getLoanStatus() == null) {
            return List.of();
        }
        return store.stream()
                .filter(stored -> loan.getLoanStatus().equals(stored.getLoanStatus()))
                .toList();
    }

    @Override
    public void update(Loan loan) {
    }
}