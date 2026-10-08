package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanOperationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ConsultLoanService {

    private static final Logger log = LoggerFactory.getLogger(ConsultLoanService.class);

    private final LoanRepositoryPort loanRepositoryPort;
    private final AuthorizeLoanOperationService authorizeLoanOperationService;

    public Loan consult(User user, Loan loan) {
        Loan stored = requireAuthoritativeLoan(loan);
        authorizeLoanOperationService.execute(user, stored);
        return stored;
    }

    public Loan consultDetails(User user, Loan loan) {
        return consult(user, loan);
    }

    private Loan requireAuthoritativeLoan(Loan loan) {
        String loanId = loan != null ? loan.getIdentifier() : null;
        Optional<Loan> found = loanRepositoryPort.findByIdentifier(loan);
        if (found.isEmpty()) {
            log.warn("Préstamo no encontrado: loanId='{}'. Verifique que el préstamo exista en 'loans'.",
                    loanId);
            throw new EntityNotFoundException("Loan", loanId);
        }
        return found.get();
    }
}