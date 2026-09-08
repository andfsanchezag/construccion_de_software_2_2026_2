package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.User;

/**
 * Input Port: Open Bank Account use case.
 */
public interface OpenBankAccountUseCase {

    BankAccount open(User requestingUser, BankAccount account);
}