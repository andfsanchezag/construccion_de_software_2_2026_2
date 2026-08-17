package application.domain.services.loan;

import application.domain.exceptions.DomainException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ValidateLoanEligibilityService {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    public boolean execute(Loan loan) {
        validateApplicantStatus(loan);
        validateRequestedAmount(loan);
        if (loan.getDestinationAccount() != null) {
            validateDestinationAccount(loan);
        }
        return true;
    }

    private void validateApplicantStatus(Loan loan) {
        Optional<Customer> customerOpt = customerRepositoryPort.findByIdentification(loan.getApplicant());
        if (customerOpt.isEmpty()) {
            throw new DomainException("Loan applicant is not eligible.");
        }
        Customer customer = customerOpt.get();
        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
            throw new DomainException("Loan applicant is not eligible.");
        }
    }

    private void validateRequestedAmount(Loan loan) {
        if (loan.getRequestedAmount() == null || loan.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Requested loan amount must be positive.");
        }
    }

    private void validateDestinationAccount(Loan loan) {
        Optional<BankAccount> accountOpt = bankAccountRepositoryPort.findByIdentifier(loan.getDestinationAccount());
        if (accountOpt.isEmpty()) {
            throw new DomainException("Destination account is not eligible.");
        }
        BankAccount account = accountOpt.get();
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DomainException("Destination account is not eligible.");
        }
    }
}
