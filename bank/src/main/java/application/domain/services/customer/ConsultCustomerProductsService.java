package application.domain.services.customer;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.ConsultCustomerProductsUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Retrieves the banking products associated with a customer: BankAccount, Loan
 * and Transfer Domain Models (customer-services.md 12).
 *
 * <p>Read-only use case: validate the domain representation, validate customer
 * existence, authorize the actor and retrieve each product through its dedicated
 * Output Port. Empty collections are valid results. The Customer Domain Model
 * exposes the loaded collections (Domain Model.md - Customer), and the canonical
 * {@code CustomerProducts} aggregate is returned to the application boundary.
 */
@Service
@RequiredArgsConstructor
public class ConsultCustomerProductsService implements ConsultCustomerProductsUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final LoanRepositoryPort loanRepositoryPort;
    private final TransferRepositoryPort transferRepositoryPort;
    private final AuthorizeCustomerOperationService authorizeCustomerOperationService;

    @Override
    public CustomerProducts consultCustomerProducts(User user, Customer customer) {
        validateInput(customer);
        Customer persisted = requireExistingCustomer(customer);
        authorizeCustomerOperationService.execute(user, persisted);

        List<BankAccount> accounts = bankAccountRepositoryPort.findByOwner(persisted);
        List<Loan> loans = loanRepositoryPort.findByApplicant(persisted);
        List<Transfer> transfers = findTransfers(accounts);

        CustomerProducts products = new CustomerProducts(accounts, loans, transfers);
        populateCustomerCollections(persisted, products);
        return products;
    }

    private void validateInput(Customer customer) {
        if (customer == null) {
            throw new InvalidCustomerException("Customer must be provided.");
        }
        if (customer.getIdentification() == null || customer.getIdentification().isBlank()) {
            throw new InvalidCustomerException("Customer identification must be provided.");
        }
    }

    private Customer requireExistingCustomer(Customer customer) {
        Optional<Customer> found = customerRepositoryPort.findByIdentification(customer);
        if (found.isEmpty()) {
            throw new CustomerNotFoundException(
                    "Customer with identification " + customer.getIdentification() + " was not found.");
        }
        return found.get();
    }

    /**
     * Transfers involving the customer's accounts, as source or destination,
     * deduplicated by product identifier.
     */
    private List<Transfer> findTransfers(List<BankAccount> accounts) {
        Set<String> seen = new HashSet<>();
        List<Transfer> transfers = new ArrayList<>();
        for (BankAccount account : accounts) {
            collectUnique(transfers, seen, transferRepositoryPort.findBySourceAccount(account));
            collectUnique(transfers, seen, transferRepositoryPort.findByDestinationAccount(account));
        }
        return transfers;
    }

    private void collectUnique(List<Transfer> transfers, Set<String> seen, List<Transfer> candidates) {
        for (Transfer transfer : candidates) {
            String key = transfer.getIdentifier();
            if (transfer.getIdentifier() == null || seen.add(key)) {
                transfers.add(transfer);
            }
        }
    }

    private void populateCustomerCollections(Customer persisted, CustomerProducts products) {
        persisted.setAccounts(products.getAccounts());
        persisted.setLoans(products.getLoans());
        persisted.setTransfers(products.getTransfers());
    }
}