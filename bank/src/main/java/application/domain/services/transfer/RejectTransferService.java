package application.domain.services.transfer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RejectTransferService {

    private static final Set<TransferStatus> REJECTABLE_STATUSES = Set.of(
            TransferStatus.PENDING, TransferStatus.WAITING_FOR_APPROVAL
    );

    private final TransferRepositoryPort transferRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        Transfer stored = transferRepositoryPort.findByIdentifier(transfer)
                .orElseThrow(() -> new EntityNotFoundException("Transfer"));
        if (!REJECTABLE_STATUSES.contains(stored.getTransferStatus())) {
            throw new DomainException("Transfer cannot be rejected from status " + stored.getTransferStatus().getCode());
        }
        String previousStatus = stored.getTransferStatus().getCode();
        stored.setTransferStatus(TransferStatus.REJECTED);
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_REJECTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of(
                "previousStatus", previousStatus,
                "newStatus", TransferStatus.REJECTED.getCode()
        ));
        return stored;
    }
}
