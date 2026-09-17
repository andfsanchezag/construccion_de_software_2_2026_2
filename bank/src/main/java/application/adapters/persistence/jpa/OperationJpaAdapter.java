package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import application.adapters.persistence.jpa.mappers.OperationJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOperationRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.models.BankingProduct;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Operation backed by Spring Data JPA (MySQL).
 */
@Repository
public class OperationJpaAdapter implements OperationRepositoryPort {

    private final SpringDataJpaOperationRepository operationRepository;
    private final SpringDataJpaUserRepository userRepository;

    public OperationJpaAdapter(SpringDataJpaOperationRepository operationRepository,
                               SpringDataJpaUserRepository userRepository) {
        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Operation save(Operation operation) {
        if (operation == null) {
            return null;
        }
        OperationJpaEntity saved = operationRepository.save(OperationJpaMapper.toEntity(operation));
        return toDomain(saved);
    }

    @Override
    public Optional<Operation> findById(Operation operation) {
        if (operation == null || operation.getOperationId() == null) {
            return Optional.empty();
        }
        return operationRepository.findById(operation.getOperationId()).map(this::toDomain);
    }

    @Override
    public List<Operation> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        return operationRepository.findByPerformedByUserId(user.getUserId()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Operation> findByProduct(BankingProduct product) {
        if (product == null || product.getIdentifier() == null) {
            return List.of();
        }
        return operationRepository.findByAffectedProductIdentifier(product.getIdentifier()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Operation> findByType(Operation operation) {
        if (operation == null || operation.getOperationType() == null) {
            return List.of();
        }
        return operationRepository.findByOperationType(operation.getOperationType().getCode()).stream()
                .map(this::toDomain)
                .toList();
    }

    private Operation toDomain(OperationJpaEntity entity) {
        User performedBy = entity.getPerformedByUserId() == null
                ? null
                : userRepository.findById(entity.getPerformedByUserId())
                        .map(userEntity -> UserJpaMapper.toDomain(userEntity, null))
                        .orElse(null);
        BankingProduct affectedProduct = OperationJpaMapper.toProductReference(
                entity.getAffectedProductType(), entity.getAffectedProductIdentifier());
        return OperationJpaMapper.toDomain(entity, performedBy, affectedProduct);
    }
}
