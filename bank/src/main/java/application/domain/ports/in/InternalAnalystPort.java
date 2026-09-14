package application.domain.ports.in;

import application.domain.models.AuditLog;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.UserStatus;

import java.util.List;

public interface InternalAnalystPort {

    User registerEmployeeUser(User user, User newEmployee);

    Customer changeCustomerStatus(User user, Customer customer, CustomerStatus newStatus);

    User changeUserStatus(User user, User targetUser, UserStatus newStatus);

    Loan approveLoan(User user, Loan loan);

    Loan rejectLoan(User user, Loan loan);

    Loan disburseLoan(User user, Loan loan, BankAccount destinationAccount);

    Loan closeLoan(User user, Loan loan);

    List<AuditLog> consultAuditLog(User user);

    List<Operation> consultAllOperations(User user);
}