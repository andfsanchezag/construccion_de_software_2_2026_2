package application.domain.models;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.InsufficientBalanceException;
import application.domain.exceptions.InvalidAccountStatusException;
import application.domain.exceptions.InvalidBankAccountException;
import application.domain.exceptions.InvalidDepositException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.InvalidWithdrawalException;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BankAccount extends BankingProduct {
    private AccountType accountType;
    private Customer owner;
    private BigDecimal currentBalance;
    private Currency currency;
    private AccountStatus accountStatus;
    private LocalDate openingDate;

    /**
     * Initializes the account as a new banking product.
     */
    public void open(LocalDate openingDate) {
        if (currentBalance == null) {
            this.currentBalance = BigDecimal.ZERO;
        }
        if (currentBalance.signum() < 0) {
            throw new InvalidBankAccountException("Initial balance must not be negative.");
        }
        this.openingDate = openingDate;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    /**
     * Deposits funds through valid domain behavior.
     */
    public void deposit(Money money) {
        if (money == null) {
            throw new InvalidDepositException("Deposit amount must be provided.");
        }
        requireOperationalStatus("Deposits");
        if (!money.isPositive()) {
            throw new InvalidDepositException("Deposit amount must be greater than zero.");
        }
        requireCompatibleCurrency(money, "deposit");
        this.currentBalance = (currentBalance == null ? BigDecimal.ZERO : currentBalance).add(money.getAmount());
    }

    /**
     * Withdraws funds through valid domain behavior.
     */
    public void withdraw(Money money) {
        if (money == null) {
            throw new InvalidWithdrawalException("Withdrawal amount must be provided.");
        }
        requireOperationalStatus("Withdrawals");
        if (!money.isPositive()) {
            throw new InvalidWithdrawalException("Withdrawal amount must be greater than zero.");
        }
        requireCompatibleCurrency(money, "withdrawal");
        if (currentBalance == null || currentBalance.compareTo(money.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }
        this.currentBalance = currentBalance.subtract(money.getAmount());
    }

    /**
     * Transitions the account to BLOCKED using valid domain behavior.
     */
    public void block() {
        if (!AccountStatus.ACTIVE.equals(accountStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), AccountStatus.BLOCKED.getCode());
        }
        this.accountStatus = AccountStatus.BLOCKED;
    }

    /**
     * Transitions the account back to ACTIVE using valid domain behavior.
     */
    public void unblock() {
        if (!AccountStatus.BLOCKED.equals(accountStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), AccountStatus.ACTIVE.getCode());
        }
        this.accountStatus = AccountStatus.ACTIVE;
    }

    /**
     * Closes the account using valid domain behavior.
     */
    public void close() {
        if (AccountStatus.CLOSED.equals(accountStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), AccountStatus.CLOSED.getCode());
        }
        if (currentBalance == null || currentBalance.compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidBankAccountException("Account cannot be closed with a non-zero balance.");
        }
        this.accountStatus = AccountStatus.CLOSED;
    }

    private void requireOperationalStatus(String operation) {
        if (!AccountStatus.ACTIVE.equals(accountStatus)) {
            throw new InvalidAccountStatusException(
                    operation + " are not allowed on an account with status " + statusCode() + ".");
        }
    }

    private void requireCompatibleCurrency(Money money, String operation) {
        if (currency == null || !money.hasSameCurrency(currency)) {
            String amountCurrency = money.getCurrency() == null ? "UNKNOWN" : money.getCurrency().getCode();
            String accountCurrency = currency == null ? "UNKNOWN" : currency.getCode();
            throw new DomainException("Currency mismatch for " + operation + ": account currency "
                    + accountCurrency + " vs amount currency " + amountCurrency + ".");
        }
    }

    private String statusCode() {
        return accountStatus == null ? "UNKNOWN" : accountStatus.getCode();
    }
}
