package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.models.BusinessCustomer;

import java.util.List;

public interface BusinessCustomerPort {

    BusinessCustomer consultCompanyProfile(User user);

    CustomerProducts consultCompanyProducts(User user);

    User registerCompanyUser(User user, User newCompanyUser);

    List<BankAccount> consultCompanyAccounts(User user);

    Loan requestCompanyLoan(User user, Loan loan);

    Transfer approveCompanyTransfer(User user, Transfer transfer);

    Transfer rejectCompanyTransfer(User user, Transfer transfer);
}