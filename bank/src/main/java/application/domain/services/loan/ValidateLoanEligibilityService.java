package application.domain.services.loan;

import application.domain.exceptions.DomainException;
import application.domain.models.Loan;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ValidateLoanEligibilityService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    public boolean execute(Loan loan) {
        validateApplicantStatus(loan);
        validateRequestedAmount(loan);
        if (loan.getDestinationAccount() != null) {
            validateDestinationAccount(loan);
        }
        return true;
    }

    private void validateApplicantStatus(Loan loan) {
        customerRepositoryPort.findByIdentification(loan.getApplicant())
                .filter(c -> CustomerStatus.ACTIVE.equals(c.getStatus()))
                .orElseThrow(() -> new DomainException("Loan applicant is not eligible."));
    }

    private void validateRequestedAmount(Loan loan) {
        if (loan.getRequestedAmount() == null || loan.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Requested loan amount must be positive.");
        }
    }

    private void validateDestinationAccount(Loan loan) {
        bankAccountRepositoryPort.findByIdentifier(loan.getDestinationAccount())
                .filter(a -> AccountStatus.ACTIVE.equals(a.getAccountStatus()))
                .orElseThrow(() -> new DomainException("Destination account is not eligible."));
    }
}
