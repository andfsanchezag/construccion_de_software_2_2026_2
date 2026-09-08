package application.adapters.persistence.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.ports.out.BankAccountRepositoryPort;

/**
 * In-memory persistence adapter for BankAccount.
 *
 * It stays inside the adapter layer and implements the BankAccountRepositoryPort,
 * so the domain never depends on a concrete store. Replace this class with a real
 * SQL adapter without touching the domain.
 */
@Service
public class BankAccountRepositoryAdapter implements BankAccountRepositoryPort {

    private final Map<String, BankAccount> store = new HashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    @Override
    public BankAccount save(BankAccount account) {
        if (account.getIdentifier() == null || account.getIdentifier().isBlank()) {
            account.setIdentifier("BA-" + sequence.incrementAndGet());
        }
        store.put(account.getIdentifier(), account);
        return account;
    }

    @Override
    public Optional<BankAccount> findByIdentifier(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(account.getIdentifier()));
    }

    @Override
    public List<BankAccount> findByOwner(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return List.of();
        }
        List<BankAccount> result = new ArrayList<>();
        for (BankAccount account : store.values()) {
            if (account.getOwner() != null
                    && customer.getIdentification().equals(account.getOwner().getIdentification())) {
                result.add(account);
            }
        }
        return result;
    }

    @Override
    public boolean existsForOwner(BankAccount account) {
        if (account == null || account.getOwner() == null || account.getOwner().getIdentification() == null) {
            return false;
        }
        return store.values().stream()
                .anyMatch(stored -> stored.getOwner() != null
                        && account.getOwner().getIdentification().equals(stored.getOwner().getIdentification())
                        && (account.getAccountType() == null
                            || account.getAccountType().equals(stored.getAccountType())));
    }

    @Override
    public void update(BankAccount account) {
        if (account != null && account.getIdentifier() != null) {
            store.put(account.getIdentifier(), account);
        }
    }
}
