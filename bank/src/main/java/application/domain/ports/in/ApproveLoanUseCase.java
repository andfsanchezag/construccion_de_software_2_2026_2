package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface ApproveLoanUseCase {

    Loan approve(User user, Loan loan);
}