package application.domain.services.transfer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExecuteTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Transfer execute(User user, Transfer transfer) {
        if (transfer == null) {
            throw new EntityNotFoundException("Transfer");
        }
        // Enrich executing actor (§7.3, same principle as CreateTransferService).
        User storedUser = resolveUser(user);
        Optional<Transfer> storedOpt = transferRepositoryPort.findByIdentifier(transfer);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        Transfer stored = storedOpt.get();
        if (!TransferStatus.APPROVED.equals(stored.getTransferStatus())) {
            throw new DomainException("Only approved transfers can be executed.");
        }
        if (stored.getAmount() == null) {
            throw new DomainException("Transfer amount must be provided.");
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
        validateAccounts(source, destination);
        BigDecimal balanceBeforeOrigin = source.getCurrentBalance();
        BigDecimal balanceBeforeDestination = destination.getCurrentBalance();
        // Domain behavior (validates ACTIVE, amount > 0, currency, balance).
        source.withdraw(Money.of(stored.getAmount(), source.getCurrency()));
        destination.deposit(Money.of(stored.getAmount(), destination.getCurrency()));
        bankAccountRepositoryPort.update(source);
        bankAccountRepositoryPort.update(destination);
        stored.markExecuted();
        transferRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_EXECUTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(storedUser);
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

    private User resolveUser(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("User must be provided.");
        }
        return userRepositoryPort.findById(user)
                .orElseThrow(() -> new UnauthorizedOperationException("User not found."));
    }

    private void validateAccounts(BankAccount source, BankAccount destination) {
        if (!AccountStatus.ACTIVE.equals(source.getAccountStatus())) {
            throw new DomainException("Source account is not active.");
        }
        if (!AccountStatus.ACTIVE.equals(destination.getAccountStatus())) {
            throw new DomainException("Destination account is not active.");
        }
    }
}
