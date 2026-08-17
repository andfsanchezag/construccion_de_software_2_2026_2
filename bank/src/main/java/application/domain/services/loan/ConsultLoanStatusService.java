package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.valueobjects.LoanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultLoanStatusService {

    private final LoanRepositoryPort loanRepositoryPort;

    public LoanStatus execute(User requestingUser, Loan loan) {
        return loanRepositoryPort.findByIdentifier(loan)
                .map(Loan::getLoanStatus)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
    }
}
