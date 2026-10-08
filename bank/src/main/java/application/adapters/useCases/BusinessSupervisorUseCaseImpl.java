package application.adapters.useCases;

import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.BusinessSupervisorPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.transfer.ApproveTransferService;
import application.domain.services.transfer.ConsultTransferService;
import application.domain.services.transfer.RejectTransferService;
import application.domain.services.operation.ConsultOperationsService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessSupervisorUseCaseImpl implements BusinessSupervisorPort {

    private final ConsultTransferService consultTransferService;
    private final ApproveTransferService approveTransferService;
    private final RejectTransferService rejectTransferService;
    private final ConsultOperationsService consultOperationsService;
    private final TransferRepositoryPort transferRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public List<Transfer> consultPendingTransfers(User user) {
        // Server-side authorization: only an ACTIVE BUSINESS_SUPERVISOR may list
        // transfers awaiting approval (previously unchecked, allowing any
        // authenticated user/role to read every pending transfer).
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, SystemRole.BUSINESS_SUPERVISOR);
        return transferRepositoryPort.findPendingApproval();
    }

    @Override
    public Transfer approveTransfer(User user, Transfer transfer) {
        return approveTransferService.execute(user, transfer);
    }

    @Override
    public Transfer rejectTransfer(User user, Transfer transfer) {
        return rejectTransferService.execute(user, transfer);
    }

    @Override
    public List<Operation> consultCompanyOperations(User user) {
        return consultOperationsService.executeByUser(user, user);
    }
}