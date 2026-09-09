ackage application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.in.ConsultLoanStatusUseCase;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanOperationService;
import application.domain.valueobjects.LoanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Provides the authoritative current status of a Loan, without trusting stale state.
 */
@Service
@RequiredArgsConstructor
public class ConsultLoanStatusService implements ConsultLoanStatusUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final AuthorizeLoanOperationService authorizeLoanOperationService;

    @Override
    public LoanStatus consultStatus(User user, Loan loan) {
        Optional<Loan> found = loanRepositoryPort.findByIdentifier(loan);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        Loan stored = found.get();
        authorizeLoanOperationService.execute(user, stored);
        return stored.getLoanStatus();
    }
}