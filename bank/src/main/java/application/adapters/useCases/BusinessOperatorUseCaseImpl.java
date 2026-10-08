package application.adapters.useCases;

import application.domain.models.BankAccount;
import application.domain.models.CustomerProducts;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.BusinessOperatorPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.services.customer.ConsultCustomerProductsService;
import application.domain.services.transfer.CreateTransferService;
import application.domain.services.operation.ConsultOperationsService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessOperatorUseCaseImpl implements BusinessOperatorPort {

    private static final Logger log = LoggerFactory.getLogger(BusinessOperatorUseCaseImpl.class);

    private final ConsultCustomerProductsService consultCustomerProductsService;
    private final CreateTransferService createTransferService;
    private final ConsultOperationsService consultOperationsService;
    private final TransferRepositoryPort transferRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public List<BankAccount> consultCompanyAccounts(User user) {
        CustomerProducts products = consultCustomerProductsService.consultCustomerProducts(user, user.getCustomer());
        return products.getAccounts();
    }

    @Override
    public Transfer createCompanyTransfer(User user, Transfer transfer) {
        return createTransferService.execute(transfer, user, user.getCustomer());
    }

    @Override
    public Transfer submitTransferForApproval(User user, Transfer transfer) {
        if (transfer == null || transfer.getIdentifier() == null || transfer.getIdentifier().isBlank()) {
            throw new EntityNotFoundException("Transfer", transfer != null ? transfer.getIdentifier() : null);
        }
        Transfer stored = transferRepositoryPort.findByIdentifier(transfer)
                .orElseThrow(() -> {
                    log.warn("Submit de transferencia rechazado: no existe transferId='{}'.",
                            transfer.getIdentifier());
                    return new EntityNotFoundException("Transfer", transfer.getIdentifier());
                });
        stored.markWaitingForApproval();
        transferRepositoryPort.update(stored);
        log.info("Transferencia enviada a aprobación: transferId='{}' por='{}'.",
                stored.getIdentifier(), user != null ? user.getUsername() : "UNKNOWN");
        Operation op = new Operation();
        op.setOperationType(OperationType.TRANSFER_CREATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of("action", "SUBMIT_FOR_APPROVAL"));
        return stored;
    }

    @Override
    public List<Operation> consultCompanyOperations(User user) {
        return consultOperationsService.executeByUser(user, user);
    }
}