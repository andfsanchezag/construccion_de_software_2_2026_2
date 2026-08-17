package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluateLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    public Loan execute(Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        Loan stored = storedOpt.get();
        Optional<application.domain.models.Customer> applicantOpt = customerRepositoryPort.findByIdentification(stored.getApplicant());
        if (applicantOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan applicant");
        }
        if (stored.getDestinationAccount() != null) {
            Optional<application.domain.models.BankAccount> accountOpt = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount());
            if (accountOpt.isEmpty()) {
                throw new EntityNotFoundException("Destination account");
            }
        }
        return stored;
    }
}
