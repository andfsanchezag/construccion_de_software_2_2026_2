package application.domain.ports.out;

import application.domain.models.BankAccount;
import application.domain.models.Customer;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepositoryPort {

    BankAccount save(BankAccount account);

    Optional<BankAccount> findByIdentifier(BankAccount account);

    List<BankAccount> findByOwner(Customer customer);

    boolean existsForOwner(BankAccount account);

    void update(BankAccount account);
}
