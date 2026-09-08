package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.valueobjects.Money;

/**
 * Input Port: Consult Account Balance use case.
 */
public interface ConsultAccountBalanceUseCase {

    Money consultBalance(User requestingUser, BankAccount account);
}