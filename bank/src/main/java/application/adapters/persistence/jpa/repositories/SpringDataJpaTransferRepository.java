package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.TransferJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Transfer persistence entity.
 */
public interface SpringDataJpaTransferRepository extends JpaRepository<TransferJpaEntity, String> {

    List<TransferJpaEntity> findBySourceAccountIdentifier(String sourceAccountIdentifier);

    List<TransferJpaEntity> findByDestinationAccountIdentifier(String destinationAccountIdentifier);

    List<TransferJpaEntity> findByTransferStatus(String transferStatus);

    List<TransferJpaEntity> findByTransferStatusIn(List<String> transferStatuses);
}
