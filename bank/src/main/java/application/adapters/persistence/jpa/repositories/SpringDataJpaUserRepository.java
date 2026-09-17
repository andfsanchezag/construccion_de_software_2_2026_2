package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the User persistence entity.
 */
public interface SpringDataJpaUserRepository extends JpaRepository<UserJpaEntity, Integer> {

    Optional<UserJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
