package application.domain.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.InvalidAccountStatusException;
import application.domain.exceptions.InvalidDepositException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.UserStatus;

class DepositFundsServiceTest {

    private Harness harness;

    @BeforeEach
    void setUp() {
        harness = new Harness();
    }

    private DepositFundsService service() {
        return new DepositFundsService(
                harness.bankAccountRepository,
                harness.customerRepository,
                harness.validateUserStatus,
                harness.registerOperationAndAudit);
    }

    @Test
    void depositFundsWhenCustomerOwnsActiveAccount() {
        Customer owner = AccountTestSupport.customer("2001", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-20", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(20, owner, UserStatus.ACTIVE);

        BankAccount result = service().deposit(user, owner, account,
                AccountTestSupport.money("250.00", Currency.USD));

        assertEquals(new BigDecimal("350.00"), result.getCurrentBalance());
        assertEquals(1, harness.operationRepository.findByProduct(account).size());
        assertEquals(1, harness.auditLogRepository.findByProduct(account).size());
    }

    @Test
    void depositFailsForZeroAmount() {
        Customer owner = AccountTestSupport.customer("2002", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-21", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(21, owner, UserStatus.ACTIVE);

        assertThrows(InvalidDepositException.class, () ->
                service().deposit(user, owner, account, AccountTestSupport.money("0", Currency.USD)));
    }

    @Test
    void depositFailsForNegativeAmount() {
        Customer owner = AccountTestSupport.customer("2003", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-22", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(22, owner, UserStatus.ACTIVE);

        assertThrows(DomainException.class, () ->
                service().deposit(user, owner, account, AccountTestSupport.money("-10.00", Currency.USD)));
    }

    @Test
    void depositFailsForIncompatibleCurrency() {
        Customer owner = AccountTestSupport.customer("2004", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-23", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(23, owner, UserStatus.ACTIVE);

        assertThrows(DomainException.class, () ->
                service().deposit(user, owner, account, AccountTestSupport.money("100.00", Currency.COP)));
    }

    @Test
    void depositFailsWhenAccountIsBlocked() {
        Customer owner = AccountTestSupport.customer("2005", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-24", owner, "100.00", AccountStatus.BLOCKED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(24, owner, UserStatus.ACTIVE);

        assertThrows(InvalidAccountStatusException.class, () ->
                service().deposit(user, owner, account, AccountTestSupport.money("50.00", Currency.USD)));
    }

    @Test
    void depositFailsWhenAccountIsClosed() {
        Customer owner = AccountTestSupport.customer("2006", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-25", owner, "100.00", AccountStatus.CLOSED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(25, owner, UserStatus.ACTIVE);

        assertThrows(InvalidAccountStatusException.class, () ->
                service().deposit(user, owner, account, AccountTestSupport.money("50.00", Currency.USD)));
    }

    @Test
    void depositFailsForAnotherCustomersAccount() {
        Customer owner = AccountTestSupport.customer("2007", CustomerStatus.ACTIVE);
        Customer other = AccountTestSupport.customer("2008", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        harness.customerRepository.save(other);
        BankAccount account = AccountTestSupport.account("ACC-26", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(26, other, UserStatus.ACTIVE);

        assertThrows(UnauthorizedOperationException.class, () ->
                service().deposit(user, other, account, AccountTestSupport.money("50.00", Currency.USD)));
    }

    @Test
    void depositByEmployeeOnBehalfOfOwner() {
        Customer owner = AccountTestSupport.customer("2009", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-27", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(27);

        BankAccount result = service().deposit(employee, owner, account,
                AccountTestSupport.money("50.00", Currency.USD));

        assertEquals(new BigDecimal("150.00"), result.getCurrentBalance());
    }
}
