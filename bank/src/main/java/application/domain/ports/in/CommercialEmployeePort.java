package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.User;

public interface CommercialEmployeePort {

    Customer consultCustomer(User user, Customer customer);

    Customer updateCustomer(User user, Customer customer);

    CustomerProducts consultCustomerProducts(User user, Customer customer);

    Loan requestLoanOnBehalfOfCustomer(User user, Customer customer, Loan loan);

    Loan consultLoanStatus(User user, Loan loan);

    BankAccount openBankAccount(User user, BankAccount account);
}