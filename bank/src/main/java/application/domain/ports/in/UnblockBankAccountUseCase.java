package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.User;

/**
 * Input Port: Unblock Bank Account use case.
 */
public interface UnblockBankAccountUseCase {

    BankAccount unblock(User requestingUser, BankAccount account);
}