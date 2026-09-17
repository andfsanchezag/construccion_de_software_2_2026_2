package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.CustomerJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Customer persistence entity.
 */
public interface SpringDataJpaCustomerRepository extends JpaRepository<CustomerJpaEntity, String> {

    Optional<CustomerJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
