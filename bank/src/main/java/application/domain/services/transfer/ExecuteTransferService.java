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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExecuteTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        Optional<Transfer> storedOpt = transferRepositoryPort.findByIdentifier(transfer);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        Transfer stored = storedOpt.get();
        if (!TransferStatus.APPROVED.equals(stored.getTransferStatus())) {
            throw new DomainException("Only approved transfers can be executed.");
        }
        Optional<BankAccount> sourceOpt = bankAccountRepositoryPort.findByIdentifier(stored.getSourceAccount());
        if (sourceOpt.isEmpty()) {
            throw new EntityNotFoundException("Source account");
        }
        BankAccount source = sourceOpt.get();
        Optional<BankAccount> destinationOpt = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount());
        if (destinationOpt.isEmpty()) {
            throw new EntityNotFoundException("Destination account");
        }
        BankAccount destination = destinationOpt.get();
        if (source.getCurrentBalance().compareTo(stored.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }
        BigDecimal balanceBeforeOrigin = source.getCurrentBalance();
        BigDecimal balanceBeforeDestination = destination.getCurrentBalance();
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
        Map<String, Object> details = new HashMap<>();
        details.put("amount", stored.getAmount());
        details.put("balanceBeforeOrigin", balanceBeforeOrigin);
        details.put("balanceAfterOrigin", source.getCurrentBalance());
        details.put("balanceBeforeDestination", balanceBeforeDestination);
        details.put("balanceAfterDestination", destination.getCurrentBalance());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
