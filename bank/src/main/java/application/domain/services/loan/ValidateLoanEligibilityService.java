ackage application.domain.services.loan;

import application.domain.exceptions.CurrencyMismatchException;
import application.domain.exceptions.CustomerNotEligibleException;
import application.domain.exceptions.DestinationAccountNotActiveException;
import application.domain.exceptions.DestinationAccountOwnershipException;
import application.domain.exceptions.InvalidDestinationAccountException;
import application.domain.exceptions.InvalidLoanAmountException;
import application.domain.exceptions.InvalidLoanException;
import application.domain.exceptions.InvalidLoanTermException;
import application.domain.exceptions.InvalidLoanTypeException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.ports.in.ValidateLoanEligibilityUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Determines whether the applicant and Loan application satisfy the business
 * conditions required to proceed. This is a cohesive eligibility operation, not a
 * collection of unrelated technical validations.
 *
 * <p>Eligibility does not imply approval: the final approval decision remains a
 * separate business action.
 */
@Service
@RequiredArgsConstructor
public class ValidateLoanEligibilityService implements ValidateLoanEligibilityUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    @Override
    public boolean validateEligibility(Loan loan) {
        if (loan == null) {
            throw new InvalidLoanException("Loan must be provided.");
        }
        validateApplicant(loan);
        validateLoanType(loan);
        validateRequestedAmount(loan);
        validateTerm(loan);
        validateCurrency(loan);
        if (loan.getDestinationAccount() != null) {
            validateDestinationAccount(loan);
        }
        return true;
    }

    private void validateApplicant(Loan loan) {
        Optional<Customer> customerOpt = customerRepositoryPort.findByIdentification(loan.getApplicant());
        if (customerOpt.isEmpty()) {
            throw new CustomerNotEligibleException("Loan applicant is not eligible.");
        }
        Customer customer = customerOpt.get();
        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
            throw new CustomerNotEligibleException(
                    "Customer " + customer.getIdentification() + " is not eligible for a loan.");
        }
    }

    private void validateLoanType(Loan loan) {
        if (loan.getLoanType() == null) {
            throw new InvalidLoanTypeException("Loan type must be provided.");
        }
    }

    private void validateRequestedAmount(Loan loan) {
        if (loan.getRequestedAmount() == null || loan.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAmountException("Requested amount must be greater than zero.");
        }
    }

    private void validateTerm(Loan loan) {
        if (loan.getTermInMonths() == null || loan.getTermInMonths() <= 0) {
            throw new InvalidLoanTermException("Loan term must be greater than zero.");
        }
    }

    private void validateCurrency(Loan loan) {
        if (loan.getCurrency() == null) {
            throw new InvalidLoanException("Loan currency must be provided.");
        }
    }

    private void validateDestinationAccount(Loan loan) {
        Optional<BankAccount> accountOpt = bankAccountRepositoryPort.findByIdentifier(loan.getDestinationAccount());
        if (accountOpt.isEmpty()) {
            throw new InvalidDestinationAccountException("Destination account is not eligible.");
        }
        BankAccount account = accountOpt.get();
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DestinationAccountNotActiveException("Destination account must be active.");
        }
        if (loan.getApplicant() == null
                || account.getOwner() == null
                || !loan.getApplicant().getIdentification().equals(account.getOwner().getIdentification())) {
            throw new DestinationAccountOwnershipException("Destination account must belong to the loan applicant.");
        }
        if (loan.getCurrency() == null || !loan.getCurrency().equals(account.getCurrency())) {
            throw new CurrencyMismatchException("Loan currency and destination account currency must match.");
        }
    }
}