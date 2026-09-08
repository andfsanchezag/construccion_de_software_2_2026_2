package application.adapters.persistence.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import application.domain.models.Customer;
import application.domain.ports.out.CustomerRepositoryPort;

/**
 * In-memory persistence adapter for Customer.
 *
 * It stays inside the adapter layer and implements the CustomerRepositoryPort,
 * so the domain never depends on a concrete store.
 */
@Service
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final Map<String, Customer> store = new HashMap<>();

    @Override
    public Customer save(Customer customer) {
        if (customer.getIdentification() != null) {
            store.put(customer.getIdentification(), customer);
        }
        return customer;
    }

    @Override
    public Optional<Customer> findByIdentification(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(customer.getIdentification()));
    }

    @Override
    public Optional<Customer> findByEmail(Customer customer) {
        if (customer == null || customer.getEmail() == null) {
            return Optional.empty();
        }
        return store.values().stream()
                .filter(c -> customer.getEmail().equals(c.getEmail()))
                .findFirst();
    }

    @Override
    public boolean existsByIdentification(Customer customer) {
        return customer != null && customer.getIdentification() != null
                && store.containsKey(customer.getIdentification());
    }

    @Override
    public boolean existsByEmail(Customer customer) {
        return customer != null && customer.getEmail() != null
                && store.values().stream().anyMatch(c -> customer.getEmail().equals(c.getEmail()));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(Customer customer) {
        if (customer != null && customer.getIdentification() != null) {
            store.put(customer.getIdentification(), customer);
        }
    }
}
