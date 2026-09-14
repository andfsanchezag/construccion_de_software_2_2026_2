package application.domain.ports.in;

import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;

import java.util.List;

public interface BusinessOperatorPort {

    List<BankAccount> consultCompanyAccounts(User user);

    Transfer createCompanyTransfer(User user, Transfer transfer);

    Transfer submitTransferForApproval(User user, Transfer transfer);

    List<Operation> consultCompanyOperations(User user);
}