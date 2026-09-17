package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.BankAccountJpaEntity;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;

/**
 * Bidirectional mapper between the BankAccount Domain Model and its JPA entity.
 *
 * <p>The owner aggregate is stored as a reference identifier and the Catalog Value
 * Objects (AccountType, Currency, AccountStatus) are converted to/from their codes.
 * The resolved Customer Domain Model is supplied by the adapter when reading.
 */
public final class BankAccountJpaMapper {

    private BankAccountJpaMapper() {
    }

    public static BankAccountJpaEntity toEntity(BankAccount domain) {
        if (domain == null) {
            return null;
        }
        BankAccountJpaEntity entity = new BankAccountJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setAccountType(domain.getAccountType() != null ? domain.getAccountType().getCode() : null);
        entity.setOwnerIdentification(
                domain.getOwner() != null ? domain.getOwner().getIdentification() : null);
        entity.setCurrentBalance(domain.getCurrentBalance());
        entity.setCurrency(domain.getCurrency() != null ? domain.getCurrency().getCode() : null);
        entity.setAccountStatus(domain.getAccountStatus() != null ? domain.getAccountStatus().getCode() : null);
        entity.setOpeningDate(domain.getOpeningDate());
        return entity;
    }

    public static BankAccount toDomain(BankAccountJpaEntity entity, Customer owner) {
        if (entity == null) {
            return null;
        }
        BankAccount domain = new BankAccount();
        domain.setIdentifier(entity.getIdentifier());
        domain.setAccountType(accountType(entity.getAccountType()));
        domain.setOwner(owner);
        domain.setCurrentBalance(entity.getCurrentBalance());
        domain.setCurrency(currency(entity.getCurrency()));
        domain.setAccountStatus(accountStatus(entity.getAccountStatus()));
        domain.setOpeningDate(entity.getOpeningDate());
        return domain;
    }

    public static AccountType accountType(String code) {
        if (code == null) {
            return null;
        }
        if (AccountType.SAVINGS.getCode().equals(code)) {
            return AccountType.SAVINGS;
        }
        if (AccountType.CHECKING.getCode().equals(code)) {
            return AccountType.CHECKING;
        }
        if (AccountType.BUSINESS.getCode().equals(code)) {
            return AccountType.BUSINESS;
        }
        return null;
    }

    public static AccountStatus accountStatus(String code) {
        if (code == null) {
            return null;
        }
        if (AccountStatus.ACTIVE.getCode().equals(code)) {
            return AccountStatus.ACTIVE;
        }
        if (AccountStatus.BLOCKED.getCode().equals(code)) {
            return AccountStatus.BLOCKED;
        }
        if (AccountStatus.CLOSED.getCode().equals(code)) {
            return AccountStatus.CLOSED;
        }
        return null;
    }

    public static Currency currency(String code) {
        if (code == null) {
            return null;
        }
        if (Currency.COP.getCode().equals(code)) {
            return Currency.COP;
        }
        if (Currency.USD.getCode().equals(code)) {
            return Currency.USD;
        }
        if (Currency.EUR.getCode().equals(code)) {
            return Currency.EUR;
        }
        return null;
    }
}
