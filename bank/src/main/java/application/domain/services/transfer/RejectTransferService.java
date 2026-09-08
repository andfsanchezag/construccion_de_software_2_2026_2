package application.domain.services.transfer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.services.authorization.AuthorizeTransferApprovalService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RejectTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final AuthorizeTransferApprovalService authorizeTransferApprovalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        if (transfer == null) {
            throw new EntityNotFoundException("Transfer");
        }
        Optional<Transfer> storedOpt = transferRepositoryPort.findByIdentifier(transfer);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        Transfer stored = storedOpt.get();
        if (stored.getTransferStatus() == null) {
            throw new DomainException("Transfer cannot be rejected from status UNKNOWN");
        }
        // Same authorization as approval (§10.4 consistent with approval process),
        // valid from PENDING or WAITING_FOR_APPROVAL (§10.5).
        User storedUser = authorizeTransferApprovalService.executeForRejection(user, stored);
        String previousStatus = stored.getTransferStatus().getCode();
        stored.markRejected();
        transferRepositoryPort.update(stored);
        LocalDateTime rejectionDate = LocalDateTime.now();
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_REJECTION);
        op.setExecutionDate(rejectionDate);
        op.setPerformedBy(storedUser);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", TransferStatus.REJECTED.getCode());
        details.put("rejectedBy", storedUser.getUsername());
        details.put("rejectionDate", rejectionDate.toString());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
