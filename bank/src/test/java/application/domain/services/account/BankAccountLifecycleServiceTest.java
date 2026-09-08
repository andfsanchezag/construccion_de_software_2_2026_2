package application.domain.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.InvalidBankAccountException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.UserStatus;

class BankAccountLifecycleServiceTest {

    private Harness harness;

    @BeforeEach
    void setUp() {
        harness = new Harness();
    }

    private BlockBankAccountService blockService() {
        return new BlockBankAccountService(
                harness.bankAccountRepository, harness.validateUserStatus, harness.registerOperationAndAudit);
    }

    private UnblockBankAccountService unblockService() {
        return new UnblockBankAccountService(
                harness.bankAccountRepository, harness.validateUserStatus, harness.registerOperationAndAudit);
    }

    private CloseBankAccountService closeService() {
        return new CloseBankAccountService(
                harness.bankAccountRepository, harness.validateUserStatus, harness.registerOperationAndAudit);
    }

    @Test
    void blockActiveAccountSucceeds() {
        Customer owner = AccountTestSupport.customer("3001", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-30", owner, "0.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(30);

        BankAccount result = blockService().block(employee, account);

        assertEquals(AccountStatus.BLOCKED, result.getAccountStatus());
        assertEquals(1, harness.operationRepository.findByProduct(account).size());
    }

    @Test
    void blockBlockedAccountIsRejected() {
        Customer owner = AccountTestSupport.customer("3002", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-31", owner, "0.00", AccountStatus.BLOCKED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(31);

        assertThrows(InvalidStatusTransitionException.class, () -> blockService().block(employee, account));
    }

    @Test
    void blockClosedAccountIsRejected() {
        Customer owner = AccountTestSupport.customer("3003", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-32", owner, "0.00", AccountStatus.CLOSED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(32);

        assertThrows(InvalidStatusTransitionException.class, () -> blockService().block(employee, account));
    }

    @Test
    void unblockBlockedAccountSucceeds() {
        Customer owner = AccountTestSupport.customer("3004", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-33", owner, "0.00", AccountStatus.BLOCKED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(33);

        BankAccount result = unblockService().unblock(employee, account);

        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        assertEquals(1, harness.operationRepository.findByProduct(account).size());
    }

    @Test
    void unblockActiveAccountIsRejected() {
        Customer owner = AccountTestSupport.customer("3005", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-34", owner, "0.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(34);

        assertThrows(InvalidStatusTransitionException.class, () -> unblockService().unblock(employee, account));
    }

    @Test
    void closeZeroBalanceAccountSucceeds() {
        Customer owner = AccountTestSupport.customer("3006", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-35", owner, "0.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(35);

        BankAccount result = closeService().close(employee, account);

        assertEquals(AccountStatus.CLOSED, result.getAccountStatus());
        assertEquals(1, harness.operationRepository.findByProduct(account).size());
    }

    @Test
    void closeNonZeroBalanceAccountIsRejected() {
        Customer owner = AccountTestSupport.customer("3007", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-36", owner, "500.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(36);

        assertThrows(InvalidBankAccountException.class, () -> closeService().close(employee, account));
    }

    @Test
    void closeAlreadyClosedAccountIsRejected() {
        Customer owner = AccountTestSupport.customer("3008", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-37", owner, "0.00", AccountStatus.CLOSED, Currency.USD);
        harness.bankAccountRepository.save(account);
        User employee = AccountTestSupport.employeeUser(37);

        assertThrows(InvalidStatusTransitionException.class, () -> closeService().close(employee, account));
    }

    @Test
    void blockByUnauthorizedCustomerIsRejected() {
        Customer owner = AccountTestSupport.customer("3009", CustomerStatus.ACTIVE);
        Customer other = AccountTestSupport.customer("3010", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-38", owner, "0.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(38, other, UserStatus.ACTIVE);

        assertThrows(UnauthorizedOperationException.class, () -> blockService().block(user, account));
    }
}
