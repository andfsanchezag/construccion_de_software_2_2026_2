package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeTransferApprovalService {

    private final ValidateBusinessSupervisorAuthorizationService validateBusinessSupervisorAuthorizationService;
    private final UserRepositoryPort userRepositoryPort;
    private final TransferRepositoryPort transferRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public void execute(User user, Transfer transfer) {

        Optional<User> storedUserOpt = userRepositoryPort.findById(user);
        if (storedUserOpt.isEmpty()) {
            throw new UnauthorizedOperationException("User not found.");
        }
        Optional<Transfer> storedTransferOpt = transferRepositoryPort.findByIdentifier(transfer);
        if (storedTransferOpt.isEmpty()) {
            throw new UnauthorizedOperationException("Transfer not found.");
        }
        user=storedUserOpt.get();
        transfer=storedTransferOpt.get();

        if(!user.getCustomer().getIdentification().equals(transfer.getSourceAccount().getOwner().getIdentification())){
            throw new UnauthorizedOperationException("The user cannot approve a transfer from their own account.");
        }
        validateBusinessSupervisorAuthorizationService.execute(user, transfer);

        transfer.setTransferStatus(TransferStatus.APPROVED);
        transferRepositoryPort.update(transfer);

        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_APPROVAL);
        op.setPerformedBy(user);
        op.setAffectedProduct(transfer);
        op.setExecutionDate(LocalDateTime.now());
        HashMap<String, Object> details = new HashMap<>();
        details.put("previousStatus", TransferStatus.WAITING_FOR_APPROVAL.getCode());
        details.put("newStatus", TransferStatus.APPROVED.getCode());
        registerOperationAndAuditService.execute(op, details);

    }
}
