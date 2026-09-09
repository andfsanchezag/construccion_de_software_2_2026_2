package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface RequestLoanUseCase {

    Loan request(User user, Loan loan);
}