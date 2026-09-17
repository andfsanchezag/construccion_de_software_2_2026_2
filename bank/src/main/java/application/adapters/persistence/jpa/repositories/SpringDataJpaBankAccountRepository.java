package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.BankAccountJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the BankAccount persistence entity.
 */
public interface SpringDataJpaBankAccountRepository extends JpaRepository<BankAccountJpaEntity, String> {

    List<BankAccountJpaEntity> findByOwnerIdentification(String ownerIdentification);

    boolean existsByOwnerIdentification(String ownerIdentification);

    boolean existsByOwnerIdentificationAndAccountType(String ownerIdentification, String accountType);
}
