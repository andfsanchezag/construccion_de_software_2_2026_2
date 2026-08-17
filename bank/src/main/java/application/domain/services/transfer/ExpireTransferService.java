package application.domain.services.transfer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.ports.out.BusinessConfigurationPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpireTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final BusinessConfigurationPort businessConfigurationPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(Transfer transfer) {
        Transfer stored = transferRepositoryPort.findByIdentifier(transfer)
                .orElseThrow(() -> new EntityNotFoundException("Transfer"));
        if (!TransferStatus.WAITING_FOR_APPROVAL.equals(stored.getTransferStatus())) {
            throw new DomainException("Only transfers awaiting approval can expire.");
        }
        validateExpirationWindow(stored);
        stored.setTransferStatus(TransferStatus.EXPIRED);
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_EXPIRATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of(
                "reason", "Approval window expired",
                "expirationDate", LocalDateTime.now().toString()
        ));
        return stored;
    }

    private void validateExpirationWindow(Transfer transfer) {
        int expirationMinutes = businessConfigurationPort.getTransferApprovalExpirationMinutes();
        LocalDateTime expiresAt = transfer.getCreationDate().plusMinutes(expirationMinutes);
        if (LocalDateTime.now().isBefore(expiresAt)) {
            throw new DomainException("Transfer approval window has not yet expired.");
        }
    }
}
