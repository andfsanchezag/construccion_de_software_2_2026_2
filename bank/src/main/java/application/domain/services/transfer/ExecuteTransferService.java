package application.domain.services.transfer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InsufficientBalanceException;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
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
public class ExecuteTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        Transfer stored = transferRepositoryPort.findByIdentifier(transfer)
                .orElseThrow(() -> new EntityNotFoundException("Transfer"));
        if (!TransferStatus.APPROVED.equals(stored.getTransferStatus())) {
            throw new DomainException("Only approved transfers can be executed.");
        }
        BankAccount source = bankAccountRepositoryPort.findByIdentifier(stored.getSourceAccount())
                .orElseThrow(() -> new EntityNotFoundException("Source account"));
        BankAccount destination = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount())
                .orElseThrow(() -> new EntityNotFoundException("Destination account"));
        if (source.getCurrentBalance().compareTo(stored.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }
        var balanceBeforeOrigin = source.getCurrentBalance();
        var balanceBeforeDestination = destination.getCurrentBalance();
        source.setCurrentBalance(source.getCurrentBalance().subtract(stored.getAmount()));
        destination.setCurrentBalance(destination.getCurrentBalance().add(stored.getAmount()));
        bankAccountRepositoryPort.update(source);
        bankAccountRepositoryPort.update(destination);
        stored.setTransferStatus(TransferStatus.EXECUTED);
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_EXECUTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of(
                "amount", stored.getAmount(),
                "balanceBeforeOrigin", balanceBeforeOrigin,
                "balanceAfterOrigin", source.getCurrentBalance(),
                "balanceBeforeDestination", balanceBeforeDestination,
                "balanceAfterDestination", destination.getCurrentBalance()
        ));
        return stored;
    }
}
