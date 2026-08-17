package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.valueobjects.LoanStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultLoanStatusService {

    private final LoanRepositoryPort loanRepositoryPort;

    public LoanStatus execute(User requestingUser, Loan loan) {
        Optional<Loan> found = loanRepositoryPort.findByIdentifier(loan);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        return found.get().getLoanStatus();
    }
}
