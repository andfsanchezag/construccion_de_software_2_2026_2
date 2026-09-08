package application.domain.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.UserStatus;

class ConsultBankAccountServiceTest {

    private Harness harness;

    @BeforeEach
    void setUp() {
        harness = new Harness();
    }

    private ConsultBankAccountService service() {
        return new ConsultBankAccountService(harness.bankAccountRepository, harness.validateUserStatus);
    }

    @Test
    void consultAccountForOwnerSucceeds() {
        Customer owner = AccountTestSupport.customer("6001", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-60", owner, "0.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(60, owner, UserStatus.ACTIVE);

        BankAccount result = service().consult(user, account);

        assertEquals("ACC-60", result.getIdentifier());
    }

    @Test
    void consultAccountForUnauthorizedCustomerIsRejected() {
        Customer owner = AccountTestSupport.customer("6002", CustomerStatus.ACTIVE);
        Customer other = AccountTestSupport.customer("6003", CustomerStatus.ACTIVE);
        BankAccount account = AccountTestSupport.account("ACC-61", owner, "0.00", AccountStatus.ACTIVE, Currency.USD);
        harness.bankAccountRepository.save(account);
        User user = AccountTestSupport.customerUser(61, other, UserStatus.ACTIVE);

        assertThrows(UnauthorizedOperationException.class, () -> service().consult(user, account));
    }
}
