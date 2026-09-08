package application.domain.services.transfer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.BusinessConfigurationPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final BusinessConfigurationPort businessConfigurationPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;
    private final UserRepositoryPort userRepositoryPort;

    public Transfer execute(Transfer transfer, User user, Customer customer) {
        
        Optional<User> storedUserOpt = userRepositoryPort.findById(user);
        if (storedUserOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found.");
        }
        user=storedUserOpt.get();
        customer = requestedCustomer(customer, user);

        BankAccount source = bankAccountRepositoryPort.findByIdentifier(transfer.getSourceAccount())
                .orElseThrow(() -> new EntityNotFoundException("Source account"));
        BankAccount destination = bankAccountRepositoryPort.findByIdentifier(transfer.getDestinationAccount())
                .orElseThrow(() -> new EntityNotFoundException("Destination account"));
        validateAccounts(source, destination, transfer.getAmount());
        if(!source.getOwner().getIdentification().equals(customer.getIdentification())){
            throw new DomainException("The provided customer does not own the source bank account.");
        }
        transfer.setCreationDate(LocalDateTime.now());
        transfer.setTransferStatus(requiresApproval(transfer.getAmount())
                ? TransferStatus.WAITING_FOR_APPROVAL
                : TransferStatus.APPROVED);
        Transfer saved = transferRepositoryPort.save(transfer);
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_CREATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(transfer.getCreatedBy());
        op.setAffectedProduct(saved);
        registerOperationAndAuditService.execute(op, Map.of(
                "amount", transfer.getAmount(),
                "status", saved.getTransferStatus().getCode()
        ));
        return saved;
    }

    private void validateAccounts(BankAccount source, BankAccount destination, BigDecimal amount) {
        if (!AccountStatus.ACTIVE.equals(source.getAccountStatus())) {
            throw new DomainException("Source account is not active.");
        }
        if (!AccountStatus.ACTIVE.equals(destination.getAccountStatus())) {
            throw new DomainException("Destination account is not active.");
        }
        if (source.getIdentifier().equals(destination.getIdentifier())) {
            throw new DomainException("Source and destination accounts must be different.");
        }
        if (source.getCurrentBalance().compareTo(amount) < 0) {
            throw new application.domain.exceptions.InsufficientBalanceException();
        }
    }

    private boolean requiresApproval(BigDecimal amount) {
        return amount.compareTo(businessConfigurationPort.getTransferApprovalThreshold()) > 0;
    }

    private Customer requestedCustomer(Customer customer, User user) {
        if(customer == null && user.getCustomer() == null){
            throw new DomainException("Either customer or requesting user must be provided.");
        }
        if(customer == null){
            customer = user.getCustomer();
        }
        return customer;
    }
}
