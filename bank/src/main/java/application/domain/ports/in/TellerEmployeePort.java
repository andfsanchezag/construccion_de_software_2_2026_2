package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.Money;

public interface TellerEmployeePort {

    Customer consultCustomer(User user, Customer customer);

    BankAccount openBankAccount(User user, BankAccount account);

    BankAccount consultBankAccount(User user, BankAccount account);

    Money consultAccountBalance(User user, BankAccount account);

    BankAccount depositFunds(User user, BankAccount account, Money amount);

    BankAccount withdrawFunds(User user, BankAccount account, Money amount);

    BankAccount blockBankAccount(User user, BankAccount account);

    BankAccount unblockBankAccount(User user, BankAccount account);

    BankAccount closeBankAccount(User user, BankAccount account);
}