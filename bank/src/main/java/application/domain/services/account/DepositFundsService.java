package application.domain.services.account;

import application.domain.exceptions.CustomerNotEligibleException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.DepositFundsUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepositFundsService implements DepositFundsUseCase {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public BankAccount deposit(User requestingUser, Customer customer, BankAccount account, Money amount) {
        validateUser(requestingUser);

        Optional<BankAccount> storedOpt = bankAccountRepositoryPort.findByIdentifier(account);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("BankAccount");
        }
        BankAccount stored = storedOpt.get();

        Customer involvedCustomer = validateDepositAccess(requestingUser, customer, stored);

        BigDecimal balanceBefore = stored.getCurrentBalance();
        stored.deposit(amount);

        bankAccountRepositoryPort.update(stored);
        registerDepositOperation(requestingUser, stored, amount, balanceBefore, involvedCustomer);
        return stored;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
    }

    private Customer validateDepositAccess(User user, Customer customer, BankAccount stored) {
        Customer involved = resolveCustomer(customer, user);
        Optional<Customer> authoritativeOpt = customerRepositoryPort.findByIdentification(involved);
        if (authoritativeOpt.isEmpty()) {
            throw new EntityNotFoundException("Customer");
        }
        Customer authoritative = authoritativeOpt.get();
        if (!CustomerStatus.ACTIVE.equals(authoritative.getStatus())) {
            throw new CustomerNotEligibleException(
                    "Customer " + authoritative.getIdentification() + " is not eligible for this operation.");
        }
        if (stored.getOwner() == null
                || !authoritative.getIdentification().equals(stored.getOwner().getIdentification())) {
            throw new UnauthorizedOperationException("The provided customer does not own the bank account.");
        }
        if (!isEmployee(user)
                && (user.getCustomer() == null
                    || !user.getCustomer().getIdentification().equals(authoritative.getIdentification()))) {
            throw new UnauthorizedOperationException("User is not authorized to operate for the given customer.");
        }
        return authoritative;
    }

    private Customer resolveCustomer(Customer customer, User user) {
        if (customer != null) {
            return customer;
        }
        if (user.getCustomer() != null) {
            return user.getCustomer();
        }
        throw new UnauthorizedOperationException(
                "Either a customer or a requesting user with an associated customer must be provided.");
    }

    private boolean isEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }

    private void registerDepositOperation(User user, BankAccount account, Money amount,
                                          BigDecimal balanceBefore, Customer customer) {
        Operation op = new Operation();
        op.setOperationType(OperationType.DEPOSIT);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(account);
        Map<String, Object> details = new HashMap<>();
        details.put("customer", customer.getIdentification());
        details.put("amount", amount.getAmount());
        details.put("currency", amount.getCurrency().getCode());
        details.put("balanceBefore", balanceBefore);
        details.put("balanceAfter", account.getCurrentBalance());
        registerOperationAndAuditService.execute(op, details);
    }
}
