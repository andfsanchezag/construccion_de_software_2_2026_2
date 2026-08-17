package application.domain.ports.out;

import application.domain.models.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Optional<Customer> findByIdentification(Customer customer);

    Optional<Customer> findByEmail(Customer customer);

    boolean existsByIdentification(Customer customer);

    boolean existsByEmail(Customer customer);

    List<Customer> findAll();

    void update(Customer customer);
}
