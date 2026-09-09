package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;

public interface ConsultLoanUseCase {

    Loan consult(User user, Loan loan);
}