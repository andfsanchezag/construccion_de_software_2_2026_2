package application.domain.ports.in;

import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.valueobjects.LoanStatus;

public interface ConsultLoanStatusUseCase {

    LoanStatus consultStatus(User user, Loan loan);
}