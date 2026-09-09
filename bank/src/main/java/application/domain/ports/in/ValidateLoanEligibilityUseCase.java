package application.domain.ports.in;

import application.domain.models.Loan;

public interface ValidateLoanEligibilityUseCase {

    boolean validateEligibility(Loan loan);
}