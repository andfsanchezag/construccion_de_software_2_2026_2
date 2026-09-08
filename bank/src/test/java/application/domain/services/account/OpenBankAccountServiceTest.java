package application.domain.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.CustomerNotEligibleException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidBankAccountException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;

class OpenBankAccountServiceTest {

    private Harness harness;

    @BeforeEach
    void setUp() {
        harness = new Harness();
    }

    private OpenBankAccountService service() {
        return new OpenBankAccountService(
                harness.bankAccountRepository,
                harness.customerRepository,
                harness.validateUserStatus,
                harness.registerOperationAndAudit);
    }

    @Test
    void openAccountForEligibleCustomerInitializesValidState() {
        Customer owner = AccountTestSupport.customer("4001", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account(null, owner, "500.00", null, Currency.USD);
        User employee = AccountTestSupport.employeeUser(40);

        BankAccount saved = service().open(employee, account);

        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
        assertNotNull(saved.getOpeningDate());
        assertNotNull(saved.getIdentifier());
        assertEquals(1, harness.operationRepository.findByProduct(saved).size());
        assertEquals(1, harness.auditLogRepository.findByProduct(saved).size());
    }

    @Test
    void openFailsWhenCustomerDoesNotExist() {
        Customer owner = AccountTestSupport.customer("4002", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account(null, owner, "500.00", null, Currency.USD);
        User employee = AccountTestSupport.employeeUser(41);

        assertThrows(EntityNotFoundException.class, () -> service().open(employee, account));
    }

    @Test
    void openFailsWhenCustomerIsInactive() {
        Customer owner = AccountTestSupport.customer("4003", CustomerStatus.INACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account(null, owner, "500.00", null, Currency.USD);
        User employee = AccountTestSupport.employeeUser(42);

        assertThrows(CustomerNotEligibleException.class, () -> service().open(employee, account));
    }

    @Test
    void openFailsWithoutAccountType() {
        Customer owner = AccountTestSupport.customer("4004", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account(null, owner, "500.00", null, Currency.USD);
        account.setAccountType(null);
        User employee = AccountTestSupport.employeeUser(43);

        assertThrows(InvalidBankAccountException.class, () -> service().open(employee, account));
    }

    @Test
    void openFailsForNegativeInitialBalance() {
        Customer owner = AccountTestSupport.customer("4005", CustomerStatus.ACTIVE);
        harness.customerRepository.save(owner);
        BankAccount account = AccountTestSupport.account(null, owner, "-10.00", null, Currency.USD);
        User employee = AccountTestSupport.employeeUser(44);

        assertThrows(InvalidBankAccountException.class, () -> service().open(employee, account));
    }
}
