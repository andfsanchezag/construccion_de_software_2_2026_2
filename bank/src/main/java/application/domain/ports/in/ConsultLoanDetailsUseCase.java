package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface ConsultLoanDetailsUseCase {

    Loan consultDetails(User user, Loan loan);
}