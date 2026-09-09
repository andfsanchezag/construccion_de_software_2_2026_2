package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface RejectLoanUseCase {

    Loan reject(User user, Loan loan);
}