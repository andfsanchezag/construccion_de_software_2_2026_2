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
        Optional<Transfer> storedOpt = transferRepositoryPort.findByIdentifier(transfer);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        Transfer stored = storedOpt.get();
        authorizeTransferApprovalService.execute(user, stored);
        stored.setTransferStatus(TransferStatus.APPROVED);
        stored.setApprovalDate(LocalDateTime.now());
        stored.setApprovedBy(user);
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_APPROVAL);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", TransferStatus.WAITING_FOR_APPROVAL.getCode());
        details.put("newStatus", TransferStatus.APPROVED.getCode());
        details.put("approvalDate", stored.getApprovalDate().toString());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
