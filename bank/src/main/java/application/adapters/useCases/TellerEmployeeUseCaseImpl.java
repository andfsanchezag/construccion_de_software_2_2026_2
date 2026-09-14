package application.adapters.useCases;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.in.TellerEmployeePort;
import application.domain.services.customer.ConsultCustomerService;
import application.domain.services.account.BlockBankAccountService;
import application.domain.services.account.CloseBankAccountService;
import application.domain.services.account.ConsultAccountBalanceService;
import application.domain.services.account.ConsultBankAccountService;
import application.domain.services.account.DepositFundsService;
import application.domain.services.account.OpenBankAccountService;
import application.domain.services.account.UnblockBankAccountService;
import application.domain.services.account.WithdrawFundsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TellerEmployeeUseCaseImpl implements TellerEmployeePort {

    private final ConsultCustomerService consultCustomerService;
    private final OpenBankAccountService openBankAccountService;
    private final ConsultBankAccountService consultBankAccountService;
    private final ConsultAccountBalanceService consultAccountBalanceService;
    private final DepositFundsService depositFundsService;
    private final WithdrawFundsService withdrawFundsService;
    private final BlockBankAccountService blockBankAccountService;
    private final UnblockBankAccountService unblockBankAccountService;
    private final CloseBankAccountService closeBankAccountService;

    @Override
    public Customer consultCustomer(User user, Customer customer) {
        return consultCustomerService.consultCustomer(user, customer);
    }

    @Override
    public BankAccount openBankAccount(User user, BankAccount account) {
        return openBankAccountService.open(user, account);
    }

    @Override
    public BankAccount consultBankAccount(User user, BankAccount account) {
        return consultBankAccountService.consult(user, account);
    }

    @Override
    public application.domain.valueobjects.Money consultAccountBalance(User user, BankAccount account) {
        return consultAccountBalanceService.consultBalance(user, account);
    }

    @Override
    public BankAccount depositFunds(User user, BankAccount account, application.domain.valueobjects.Money amount) {
        return depositFundsService.deposit(user, account, amount);
    }

    @Override
    public BankAccount withdrawFunds(User user, BankAccount account, application.domain.valueobjects.Money amount) {
        return withdrawFundsService.withdraw(user, account, amount);
    }

    @Override
    public BankAccount blockBankAccount(User user, BankAccount account) {
        return blockBankAccountService.block(user, account);
    }

    @Override
    public BankAccount unblockBankAccount(User user, BankAccount account) {
        return unblockBankAccountService.unblock(user, account);
    }

    @Override
    public BankAccount closeBankAccount(User user, BankAccount account) {
        return closeBankAccountService.close(user, account);
    }
}