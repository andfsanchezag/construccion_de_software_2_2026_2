ackage application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.in.ConsultLoanDetailsUseCase;
import application.domain.ports.in.ConsultLoanUseCase;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Retrieves the authoritative Loan Domain Model.
 *
 * <p>For customer access the requesting Customer must have the required relationship
 * with the Loan; employees are authorized according to their SystemRole.
 */
@Service
@RequiredArgsConstructor
public class ConsultLoanService implements ConsultLoanUseCase, ConsultLoanDetailsUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final AuthorizeLoanOperationService authorizeLoanOperationService;

    @Override
    public Loan consult(User user, Loan loan) {
        Loan stored = requireAuthoritativeLoan(loan);
        authorizeLoanOperationService.execute(user, stored);
        return stored;
    }

    @Override
    public Loan consultDetails(User user, Loan loan) {
        return consult(user, loan);
    }

    private Loan requireAuthoritativeLoan(Loan loan) {
        Optional<Loan> found = loanRepositoryPort.findByIdentifier(loan);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        return found.get();
    }
}