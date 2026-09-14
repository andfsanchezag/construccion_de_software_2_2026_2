package application.domain.ports.in;

import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;

import java.util.List;

public interface BusinessSupervisorPort {

    List<Transfer> consultPendingTransfers(User user);

    Transfer approveTransfer(User user, Transfer transfer);

    Transfer rejectTransfer(User user, Transfer transfer);

    List<Operation> consultCompanyOperations(User user);
}