package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Operation persistence entity.
 */
public interface SpringDataJpaOperationRepository extends JpaRepository<OperationJpaEntity, Integer> {

    List<OperationJpaEntity> findByPerformedByUserId(Integer performedByUserId);

    List<OperationJpaEntity> findByAffectedProductIdentifier(String affectedProductIdentifier);

    List<OperationJpaEntity> findByOperationType(String operationType);
}
