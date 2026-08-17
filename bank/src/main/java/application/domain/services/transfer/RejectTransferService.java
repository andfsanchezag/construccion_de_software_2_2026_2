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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RejectTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        Optional<Transfer> storedOpt = transferRepositoryPort.findByIdentifier(transfer);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        Transfer stored = storedOpt.get();
        TransferStatus currentStatus = stored.getTransferStatus();
        if (!TransferStatus.PENDING.equals(currentStatus)
                && !TransferStatus.WAITING_FOR_APPROVAL.equals(currentStatus)) {
            throw new DomainException("Transfer cannot be rejected from status " + currentStatus.getCode());
        }
        String previousStatus = currentStatus.getCode();
        stored.setTransferStatus(TransferStatus.REJECTED);
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_REJECTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", TransferStatus.REJECTED.getCode());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
