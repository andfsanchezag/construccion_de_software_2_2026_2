package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Value Object representing an amount of money in a specific currency.
 *
 * Encapsulates the monetary invariants of the domain:
 * <ul>
 *   <li>amount must not be null</li>
 *   <li>amount must not be negative</li>
 *   <li>currency must not be null</li>
 * </ul>
 *
 * Transactional operations (deposit/withdraw) additionally require the amount to be
 * strictly positive, which is enforced by the domain behavior of BankAccount.
 */
@Getter
@EqualsAndHashCode
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new DomainException("Money amount must not be null.");
        }
        if (currency == null) {
            throw new DomainException("Money currency must not be null.");
        }
        if (amount.signum() < 0) {
            throw new DomainException("Money amount must not be negative.");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean hasSameCurrency(Currency other) {
        return currency.equals(other);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    private void requireSameCurrency(Money other) {
        if (!hasSameCurrency(other.currency)) {
            throw new DomainException("Currency mismatch: " + currency.getCode() + " vs " + other.currency.getCode() + ".");
        }
    }
}
