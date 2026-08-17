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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApproveTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final AuthorizeTransferApprovalService authorizeTransferApprovalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        Transfer stored = transferRepositoryPort.findByIdentifier(transfer)
                .orElseThrow(() -> new EntityNotFoundException("Transfer"));
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
        registerOperationAndAuditService.execute(op, Map.of(
                "previousStatus", TransferStatus.WAITING_FOR_APPROVAL.getCode(),
                "newStatus", TransferStatus.APPROVED.getCode(),
                "approvalDate", stored.getApprovalDate().toString()
        ));
        return stored;
    }
}
