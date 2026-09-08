package application.domain.services.transfer;

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
public class ApproveTransferService {

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
        String previousStatus = stored.getTransferStatus() == null
                ? "UNKNOWN"
                : stored.getTransferStatus().getCode();
        // Authoritative approver + BUSINESS_SUPERVISOR / ACTIVE / WAITING checks.
        // Pure validator: no persistence/audit inside (single orchestration here).
        User storedUser = authorizeTransferApprovalService.execute(user, stored);
        LocalDateTime approvalDate = LocalDateTime.now();
        stored.markApproved(storedUser, approvalDate);
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_APPROVAL);
        op.setExecutionDate(approvalDate);
        op.setPerformedBy(storedUser);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", TransferStatus.APPROVED.getCode());
        details.put("approvedBy", storedUser.getUsername());
        details.put("approvalDate", stored.getApprovalDate().toString());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
