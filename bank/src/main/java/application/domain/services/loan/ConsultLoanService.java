package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultLoanService {

    private final LoanRepositoryPort loanRepositoryPort;

    public Loan execute(User requestingUser, Loan loan) {
        return loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
    }
}
