package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.LoanJpaEntity;
import application.adapters.persistence.jpa.mappers.BankAccountJpaMapper;
import application.adapters.persistence.jpa.mappers.CustomerJpaMapper;
import application.adapters.persistence.jpa.mappers.LoanJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBankAccountRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCustomerRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaLoanRepository;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.ports.out.LoanRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Loan backed by Spring Data JPA (MySQL).
 *
 * <p>Applicant and destination account are stored as reference identifiers and resolved
 * when reading, so the domain aggregate graph never becomes an ORM mapping.
 */
@Repository
public class LoanJpaAdapter implements LoanRepositoryPort {

    private final SpringDataJpaLoanRepository loanRepository;
    private final SpringDataJpaCustomerRepository customerRepository;
    private final SpringDataJpaBankAccountRepository bankAccountRepository;

    public LoanJpaAdapter(SpringDataJpaLoanRepository loanRepository,
                          SpringDataJpaCustomerRepository customerRepository,
                          SpringDataJpaBankAccountRepository bankAccountRepository) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public Loan save(Loan loan) {
        if (loan.getIdentifier() == null || loan.getIdentifier().isBlank()) {
            loan.setIdentifier("LN-" + UUID.randomUUID());
        }
        LoanJpaEntity saved = loanRepository.save(LoanJpaMapper.toEntity(loan));
        return toDomain(saved);
    }

    @Override
    public Optional<Loan> findByIdentifier(Loan loan) {
        if (loan == null || loan.getIdentifier() == null) {
            return Optional.empty();
        }
        return loanRepository.findById(loan.getIdentifier()).map(this::toDomain);
    }

    @Override
    public List<Loan> findByApplicant(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return List.of();
        }
        return loanRepository.findByApplicantIdentification(customer.getIdentification()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Loan> findByStatus(Loan loan) {
        if (loan == null || loan.getLoanStatus() == null) {
            return List.of();
        }
        return loanRepository.findByLoanStatus(loan.getLoanStatus().getCode()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void update(Loan loan) {
        if (loan == null || loan.getIdentifier() == null) {
            return;
        }
        loanRepository.save(LoanJpaMapper.toEntity(loan));
    }

    private Loan toDomain(LoanJpaEntity entity) {
        Customer applicant = entity.getApplicantIdentification() == null
                ? null
                : customerRepository.findById(entity.getApplicantIdentification())
                        .map(CustomerJpaMapper::toDomain)
                        .orElse(null);
        BankAccount destinationAccount = entity.getDestinationAccountIdentifier() == null
                ? null
                : bankAccountRepository.findById(entity.getDestinationAccountIdentifier())
                        .map(accountEntity -> BankAccountJpaMapper.toDomain(accountEntity, null))
                        .orElse(null);
        return LoanJpaMapper.toDomain(entity, applicant, destinationAccount);
    }
}
