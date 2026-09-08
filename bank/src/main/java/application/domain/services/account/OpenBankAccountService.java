package application.domain.services.account;

import application.domain.exceptions.CustomerNotEligibleException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidBankAccountException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.OpenBankAccountUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OpenBankAccountService implements OpenBankAccountUseCase {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public BankAccount open(User requestingUser, BankAccount account) {
        validateUser(requestingUser);
        validateAccountFields(account);

        Optional<Customer> ownerOpt = customerRepositoryPort.findByIdentification(account.getOwner());
        if (ownerOpt.isEmpty()) {
            throw new EntityNotFoundException("Account owner");
        }
        Customer owner = ownerOpt.get();
        validateCustomerEligibility(owner);
        account.setOwner(owner);

        account.open(LocalDate.now());
        BankAccount saved = bankAccountRepositoryPort.save(account);
        registerOpeningOperation(requestingUser, saved);
        return saved;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
    }

    private void validateAccountFields(BankAccount account) {
        if (account == null) {
            throw new InvalidBankAccountException("Bank account must be provided.");
        }
        if (account.getOwner() == null) {
            throw new InvalidBankAccountException("Bank account owner must be provided.");
        }
        if (account.getAccountType() == null) {
            throw new InvalidBankAccountException("Bank account type must be provided.");
        }
        if (account.getCurrency() == null) {
            throw new InvalidBankAccountException("Bank account currency must be provided.");
        }
        if (account.getCurrentBalance() != null && account.getCurrentBalance().signum() < 0) {
            throw new InvalidBankAccountException("Initial balance must not be negative.");
        }
    }

    private void validateCustomerEligibility(Customer customer) {
        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
            throw new CustomerNotEligibleException(
                    "Customer " + customer.getIdentification() + " is not eligible to open a bank account.");
        }
    }

    private void registerOpeningOperation(User user, BankAccount account) {
        Operation op = new Operation();
        op.setOperationType(OperationType.ACCOUNT_OPENING);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(account);
        Map<String, Object> details = new HashMap<>();
        details.put("identifier", account.getIdentifier());
        registerOperationAndAuditService.execute(op, details);
    }
}
