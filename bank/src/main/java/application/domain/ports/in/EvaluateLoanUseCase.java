package application.domain.ports.in;

import application.domain.enums.ApprovalDecision;
import application.domain.models.Loan;

public interface EvaluateLoanUseCase {

    ApprovalDecision evaluate(Loan loan);
}