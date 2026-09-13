package application.domain.models;

import lombok.Getter;

import java.util.List;

/**
 * Aggregates the banking products associated with a Customer for the
 * Consult Customer Products use case.
 *
 * <p>All elements are Domain Models: BankAccount, Loan and Transfer.
 * Empty collections are valid results (customer-services.md 12.10).
 */
@Getter
public class CustomerProducts {

    private final List<BankAccount> accounts;
    private final List<Loan> loans;
    private final List<Transfer> transfers;

    public CustomerProducts(List<BankAccount> accounts, List<Loan> loans, List<Transfer> transfers) {
        this.accounts = accounts == null ? List.of() : accounts;
        this.loans = loans == null ? List.of() : loans;
        this.transfers = transfers == null ? List.of() : transfers;
    }
}
