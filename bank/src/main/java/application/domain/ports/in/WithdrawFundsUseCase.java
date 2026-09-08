package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.Money;

/**
 * Input Port: Withdraw Funds use case.
 */
public interface WithdrawFundsUseCase {

    BankAccount withdraw(User requestingUser, Customer customer, BankAccount account, Money amount);
}