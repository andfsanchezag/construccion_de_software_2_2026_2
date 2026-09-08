package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.User;

/**
 * Input Port: Close Bank Account use case.
 */
public interface CloseBankAccountUseCase {

    BankAccount close(User requestingUser, BankAccount account);
}