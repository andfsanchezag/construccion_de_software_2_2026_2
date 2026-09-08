package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.User;

/**
 * Input Port: Block Bank Account use case.
 */
public interface BlockBankAccountUseCase {

    BankAccount block(User requestingUser, BankAccount account);
}