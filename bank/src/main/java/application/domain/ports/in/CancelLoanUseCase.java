package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface CancelLoanUseCase {

    Loan cancel(User user, Loan loan);
}