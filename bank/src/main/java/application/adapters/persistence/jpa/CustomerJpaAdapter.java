package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.CustomerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCustomerRepository;
import application.domain.models.Customer;
import application.domain.ports.out.CustomerRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Customer backed by Spring Data JPA (MySQL).
 */
@Repository
public class CustomerJpaAdapter implements CustomerRepositoryPort {

    private final SpringDataJpaCustomerRepository customerRepository;

    public CustomerJpaAdapter(SpringDataJpaCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer save(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return customer;
        }
        return CustomerJpaMapper.toDomain(
                customerRepository.save(CustomerJpaMapper.toEntity(customer)));
    }

    @Override
    public Optional<Customer> findByIdentification(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return Optional.empty();
        }
        return customerRepository.findById(customer.getIdentification()).map(CustomerJpaMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(Customer customer) {
        if (customer == null || customer.getEmail() == null) {
            return Optional.empty();
        }
        return customerRepository.findByEmail(customer.getEmail()).map(CustomerJpaMapper::toDomain);
    }

    @Override
    public boolean existsByIdentification(Customer customer) {
        return customer != null && customer.getIdentification() != null
                && customerRepository.existsById(customer.getIdentification());
    }

    @Override
    public boolean existsByEmail(Customer customer) {
        return customer != null && customer.getEmail() != null
                && customerRepository.existsByEmail(customer.getEmail());
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll().stream().map(CustomerJpaMapper::toDomain).toList();
    }

    @Override
    public void update(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return;
        }
        customerRepository.save(CustomerJpaMapper.toEntity(customer));
    }
}
