package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.User;

/**
 * Input Port: Consult Bank Account use case.
 */
public interface ConsultBankAccountUseCase {

    BankAccount consult(User requestingUser, BankAccount account);
}