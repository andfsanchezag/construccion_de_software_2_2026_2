package application.domain.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.UserStatus;

class ConsultAccountBalanceServiceTest {

    private Harness harness;

    @BeforeEach
    void setUp() {
        harness = new Harness();
    }

    private ConsultAccountBalanceService service() {
        return new ConsultAccountBalanceService(harness.bankAccountRepository, harness.validateUserStatus);
    }

    @Test
    void consultBalanceForOwnerReturnsAuthoritativeBalance() {
        Customer owner = AccountTestSupport.customer("5001", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-50", owner, "1234.56", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(50, owner, UserStatus.ACTIVE);

        Money balance = service().consultBalance(user, account);

        assertEquals(new BigDecimal("1234.56"), balance.getAmount());
        assertEquals(Currency.USD, balance.getCurrency());
    }

    @Test
    void consultBalanceForEmployeeIsAllowed() {
        Customer owner = AccountTestSupport.customer("5002", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-51", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(51);

        Money balance = service().consultBalance(employee, account);

        assertEquals(new BigDecimal("100.00"), balance.getAmount());
    }

    @Test
    void consultBalanceForUnauthorizedCustomerIsRejected() {
        Customer owner = AccountTestSupport.customer("5003", CustomerStatus.ACTIVE);
        Customer other = AccountTestSupport.customer("5004", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-52", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(52, other, UserStatus.ACTIVE);

        assertThrows(UnauthorizedOperationException.class, () -> service().consultBalance(user, account));
    }

    @Test
    void consultBalanceWhenAccountDoesNotExist() {
        Customer owner = AccountTestSupport.customer("5005", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("UNKNOWN", owner, "100.00", AccountStatus.ACTIVE, Currency.USD);
        User employee = AccountTestSupport.employeeUser(53);

        assertThrows(EntityNotFoundException.class, () -> service().consultBalance(employee, account));
    }
}
