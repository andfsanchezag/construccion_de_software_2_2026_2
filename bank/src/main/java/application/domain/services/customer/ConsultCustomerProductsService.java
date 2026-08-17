import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultCustomerProductsService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final LoanRepositoryPort loanRepositoryPort;
    private final TransferRepositoryPort transferRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    public CustomerProducts execute(User requestingUser, Customer customer) {
        authorizeCustomerOperationService.execute(requestingUser, customer);

        List<BankAccount> accounts = bankAccountRepositoryPort.findByOwner(customer);
        List<Loan> loans = loanRepositoryPort.findByApplicant(customer);

        List<Transfer> transfers = new ArrayList<>();
        for (BankAccount account : accounts) {
            List<Transfer> accountTransfers = transferRepositoryPort.findBySourceAccount(account);
            transfers.addAll(accountTransfers);
        }

        return new CustomerProducts(accounts, loans, transfers);
    }

    @Getter
    public static final class CustomerProducts {
        private final List<BankAccount> accounts;
        private final List<Loan> loans;
        private final List<Transfer> transfers;

        public CustomerProducts(List<BankAccount> accounts, List<Loan> loans, List<Transfer> transfers) {
            this.accounts = accounts;
            this.loans = loans;
            this.transfers = transfers;
        }
    }
}

    @Getter
    public static final class CustomerProducts {
        private final List<BankAccount> accounts;
        private final List<Loan> loans;
        private final List<Transfer> transfers;

        public CustomerProducts(List<BankAccount> accounts, List<Loan> loans, List<Transfer> transfers) {
            this.accounts = accounts;
            this.loans = loans;
            this.transfers = transfers;
        }
    }
}
