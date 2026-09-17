package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.BankAccountJpaEntity;
import application.adapters.persistence.jpa.mappers.BankAccountJpaMapper;
import application.adapters.persistence.jpa.mappers.CustomerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBankAccountRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCustomerRepository;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.ports.out.BankAccountRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for BankAccount backed by Spring Data JPA (MySQL).
 *
 * <p>Implements {@link BankAccountRepositoryPort} and keeps the domain independent from
 * the ORM: it converts the domain model to/from {@link BankAccountJpaEntity} and stores
 * the owner aggregate as a reference identifier.
 */
@Repository
public class BankAccountJpaAdapter implements BankAccountRepositoryPort {

    private final SpringDataJpaBankAccountRepository bankAccountRepository;
    private final SpringDataJpaCustomerRepository customerRepository;

    public BankAccountJpaAdapter(SpringDataJpaBankAccountRepository bankAccountRepository,
                                 SpringDataJpaCustomerRepository customerRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public BankAccount save(BankAccount account) {
        if (account.getIdentifier() == null || account.getIdentifier().isBlank()) {
            account.setIdentifier("BA-" + UUID.randomUUID());
        }
        BankAccountJpaEntity saved = bankAccountRepository.save(BankAccountJpaMapper.toEntity(account));
        return toDomain(saved);
    }

    @Override
    public Optional<BankAccount> findByIdentifier(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return Optional.empty();
        }
        return bankAccountRepository.findById(account.getIdentifier()).map(this::toDomain);
    }

    @Override
    public List<BankAccount> findByOwner(Customer customer) {
        if (customer == null || customer.getIdentification() == null) {
            return List.of();
        }
        return bankAccountRepository.findByOwnerIdentification(customer.getIdentification()).stream()
                .map(entity -> BankAccountJpaMapper.toDomain(entity, customer))
                .toList();
    }

    @Override
    public boolean existsForOwner(BankAccount account) {
        if (account == null || account.getOwner() == null || account.getOwner().getIdentification() == null) {
            return false;
        }
        String ownerIdentification = account.getOwner().getIdentification();
        if (account.getAccountType() == null) {
            return bankAccountRepository.existsByOwnerIdentification(ownerIdentification);
        }
        return bankAccountRepository.existsByOwnerIdentificationAndAccountType(
                ownerIdentification, account.getAccountType().getCode());
    }

    @Override
    public void update(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return;
        }
        bankAccountRepository.save(BankAccountJpaMapper.toEntity(account));
    }

    private BankAccount toDomain(BankAccountJpaEntity entity) {
        Customer owner = entity.getOwnerIdentification() == null
                ? null
                : customerRepository.findById(entity.getOwnerIdentification())
                        .map(CustomerJpaMapper::toDomain)
                        .orElse(null);
        return BankAccountJpaMapper.toDomain(entity, owner);
    }
}
