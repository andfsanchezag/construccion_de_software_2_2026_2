package application.domain.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InsufficientBalanceException;
import application.domain.exceptions.InvalidAccountStatusException;
import application.domain.exceptions.InvalidWithdrawalException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.UserStatus;

class WithdrawFundsServiceTest {

    private Harness harness;

    @BeforeEach
    void setUp() {
        harness = new Harness();
    }

    private WithdrawFundsService service() {
        return new WithdrawFundsService(
                harness.bankAccountRepository,
                harness.customerRepository,
                harness.validateUserStatus,
                harness.registerOperationAndAudit);
    }

    @Test
    void withdrawFundsWhenCustomerOwnsActiveAccount() {
        Customer owner = AccountTestSupport.customer("1001", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-1", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(1, owner, UserStatus.ACTIVE);

        BankAccount result = service().withdraw(user, owner, account,
                AccountTestSupport.money("300.00", Currency.USD));

        assertEquals(new BigDecimal("700.00"), result.getCurrentBalance());
        assertEquals(1, harness.operationRepository.findByProduct(account).size());
        assertEquals(1, harness.auditLogRepository.findByProduct(account).size());
    }

    @Test
    void withdrawFundsUsingCustomerFromRequestingUser() {
        Customer owner = AccountTestSupport.customer("1002", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-2", owner, "500.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(2, owner, UserStatus.ACTIVE);

        BankAccount result = service().withdraw(user, null, account,
                AccountTestSupport.money("200.00", Currency.USD));

        assertEquals(new BigDecimal("300.00"), result.getCurrentBalance());
    }

    @Test
    void withdrawFailsWhenBalanceIsInsufficient() {
        Customer owner = AccountTestSupport.customer("1003", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-3", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(3, owner, UserStatus.ACTIVE);

        assertThrows(InsufficientBalanceException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("1500.00", Currency.USD)));
    }

    @Test
    void withdrawFailsForZeroAmount() {
        Customer owner = AccountTestSupport.customer("1004", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-4", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(4, owner, UserStatus.ACTIVE);

        assertThrows(InvalidWithdrawalException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("0", Currency.USD)));
    }

    @Test
    void withdrawFailsForNegativeAmount() {
        Customer owner = AccountTestSupport.customer("1005", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-5", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(5, owner, UserStatus.ACTIVE);

        assertThrows(DomainException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("-50.00", Currency.USD)));
    }

    @Test
    void withdrawFailsForIncompatibleCurrency() {
        Customer owner = AccountTestSupport.customer("1006", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-6", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(6, owner, UserStatus.ACTIVE);

        assertThrows(DomainException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("100.00", Currency.EUR)));
    }

    @Test
    void withdrawFailsWhenAccountIsBlocked() {
        Customer owner = AccountTestSupport.customer("1007", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-7", owner, "1000.00", AccountStatus.BLOCKED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(7, owner, UserStatus.ACTIVE);

        assertThrows(InvalidAccountStatusException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("100.00", Currency.USD)));
    }

    @Test
    void withdrawFailsWhenAccountIsClosed() {
        Customer owner = AccountTestSupport.customer("1008", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-8", owner, "1000.00", AccountStatus.CLOSED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(8, owner, UserStatus.ACTIVE);

        assertThrows(InvalidAccountStatusException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("100.00", Currency.USD)));
    }

    @Test
    void withdrawFailsForAnotherCustomersAccount() {
        Customer owner = AccountTestSupport.customer("1009", CustomerStatus.ACTIVE);
        Customer other = AccountTestSupport.customer("1010", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        harness.customerRepository.save(other);
        BankAccount account = AccountTestSupport.account("ACC-9", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(9, other, UserStatus.ACTIVE);

        assertThrows(UnauthorizedOperationException.class, () ->
                service().withdraw(user, other, account, AccountTestSupport.money("100.00", Currency.USD)));
    }

    @Test
    void withdrawFailsWhenUserIsInactive() {
        Customer owner = AccountTestSupport.customer("1011", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-10", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(10, owner, UserStatus.INACTIVE);

        assertThrows(UnauthorizedOperationException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("100.00", Currency.USD)));
    }

    @Test
    void withdrawFailsWhenNoCustomerCanBeResolved() {
        Customer owner = AccountTestSupport.customer("1012", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-11", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(11);

        assertThrows(UnauthorizedOperationException.class, () ->
                service().withdraw(employee, null, account, AccountTestSupport.money("100.00", Currency.USD)));
    }

    @Test
    void withdrawByEmployeeOnBehalfOfOwner() {
        Customer owner = AccountTestSupport.customer("1013", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account("ACC-12", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(12);

        BankAccount result = service().withdraw(employee, owner, account,
                AccountTestSupport.money("250.00", Currency.USD));

        assertEquals(new BigDecimal("750.00"), result.getCurrentBalance());
    }

    @Test
    void withdrawFailsWhenAccountDoesNotExist() {
        Customer owner = AccountTestSupport.customer("1014", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        User user = AccountTestSupport.customerUser(13, owner, UserStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("UNKNOWN", owner, "1000.00", AccountStatus.ACTIVE, Currency.USD);

        assertThrows(EntityNotFoundException.class, () ->
                service().withdraw(user, owner, account, AccountTestSupport.money("100.00", Currency.USD)));
    }
}
