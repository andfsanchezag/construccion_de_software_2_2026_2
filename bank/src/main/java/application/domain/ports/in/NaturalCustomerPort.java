package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.List;

public interface NaturalCustomerPort {

    Customer consultMyProfile(User user);

    Customer updateMyProfile(User user, Customer customer);

    CustomerProducts consultMyProducts(User user);

    List<BankAccount> consultMyAccounts(User user);

    Money consultAccountBalance(User user, BankAccount account);

    Loan requestLoan(User user, Loan loan);

    Loan consultLoan(User user, Loan loan);

    Loan registerLoanPayment(User user, Loan loan, Money amount);

    Transfer createTransfer(User user, Transfer transfer);

    Transfer executeTransfer(User user, Transfer transfer);

    List<Operation> consultMyOperations(User user);
}