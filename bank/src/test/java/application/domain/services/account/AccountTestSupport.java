package application.domain.services.account;

import java.math.BigDecimal;

import application.adapters.persistence.mongodb.AuditLogRepositoryAdapter;
import application.adapters.persistence.sql.BankAccountRepositoryAdapter;
import application.adapters.persistence.sql.CustomerRepositoryAdapter;
import application.adapters.persistence.sql.OperationRepositoryAdapter;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

/**
 * Shared in-memory fakes (output ports) and fixtures for Bank Account service tests.
 *
 * The in-memory repository adapters act as fakes/stubs of the output ports, so the
 * services are tested without any infrastructure (no MySQL, MongoDB, REST or JPA).
 */
final class AccountTestSupport {

    private AccountTestSupport() {
    }

    static NaturalCustomer customer(String identification, CustomerStatus status) {
        NaturalCustomer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        customer.setName("Customer " + identification);
        customer.setStatus(status);
        return customer;
    }

    static User customerUser(Integer userId, Customer customer, UserStatus status) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("customer" + userId);
        user.setStatus(status);
        user.setRole(SystemRole.NATURAL_CUSTOMER);
        user.setCustomer(customer);
        user.setIdentification(customer.getIdentification());
        return user;
    }

    static User employeeUser(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("employee" + userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(SystemRole.TELLER_EMPLOYEE);
        return user;
    }

    static BankAccount account(String identifier, Customer owner, String balance,
                               AccountStatus status, Currency currency) {
        BankAccount account = new BankAccount();
        account.setIdentifier(identifier);
        account.setOwner(owner);
        account.setCurrentBalance(new BigDecimal(balance));
        account.setCurrency(currency);
        account.setAccountStatus(status);
        account.setAccountType(AccountType.SAVINGS);
        return account;
    }

    static Money money(String amount, Currency currency) {
        return Money.of(new BigDecimal(amount), currency);
    }
}