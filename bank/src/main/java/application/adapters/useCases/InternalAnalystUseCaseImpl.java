package application.adapters.useCases;

import application.domain.models.AuditLog;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.InternalAnalystPort;
import application.domain.services.customer.ChangeCustomerStatusService;
import application.domain.services.loan.ApproveLoanService;
import application.domain.services.loan.CancelLoanService;
import application.domain.services.loan.DisburseLoanService;
import application.domain.services.loan.RejectLoanService;
import application.domain.services.operation.ConsultAuditLogsService;
import application.domain.services.operation.ConsultOperationsService;
import application.domain.services.user.ChangeUserStatusService;
import application.domain.services.user.RegisterEmployeeUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalAnalystUseCaseImpl implements InternalAnalystPort {

    private final RegisterEmployeeUserService registerEmployeeUserService;
    private final ChangeCustomerStatusService changeCustomerStatusService;
    private final ChangeUserStatusService changeUserStatusService;
    private final ApproveLoanService approveLoanService;
    private final RejectLoanService rejectLoanService;
    private final DisburseLoanService disburseLoanService;
    private final CancelLoanService closeLoanService;
    private final ConsultAuditLogsService consultAuditLogsService;
    private final ConsultOperationsService consultOperationsService;

    @Override
    public User registerEmployeeUser(User user, User newEmployee) {
        return registerEmployeeUserService.registerEmployeeUser(user, newEmployee);
    }

    @Override
    public Customer changeCustomerStatus(User user, Customer customer, application.domain.valueobjects.CustomerStatus newStatus) {
        return changeCustomerStatusService.changeStatus(user, customer, newStatus);
    }

    @Override
    public User changeUserStatus(User user, User targetUser, application.domain.valueobjects.UserStatus newStatus) {
        return changeUserStatusService.changeStatus(user, targetUser, newStatus);
    }

    @Override
    public Loan approveLoan(User user, Loan loan) {
        return approveLoanService.approve(user, loan);
    }

    @Override
    public Loan rejectLoan(User user, Loan loan) {
        return rejectLoanService.execute(user, loan);
    }

    @Override
    public Loan disburseLoan(User user, Loan loan, BankAccount destinationAccount) {
        loan.setDestinationAccount(destinationAccount);
        return disburseLoanService.execute(user, loan);
    }

    @Override
    public Loan closeLoan(User user, Loan loan) {
        return closeLoanService.execute(user, loan);
    }

    @Override
    public List<AuditLog> consultAuditLog(User user) {
        return consultAuditLogsService.findAll();
    }

    @Override
    public List<Operation> consultAllOperations(User user) {
        return consultOperationsService.findAll();
    }
}