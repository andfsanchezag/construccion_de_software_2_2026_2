package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface DisburseLoanUseCase {

    Loan disburse(User user, Loan loan);
}