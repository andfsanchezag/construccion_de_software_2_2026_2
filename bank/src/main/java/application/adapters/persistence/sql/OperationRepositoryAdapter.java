package application.adapters.persistence.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import application.domain.models.BankingProduct;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;

/**
 * In-memory persistence adapter for Operation.
 *
 * It stays inside the adapter layer and implements the OperationRepositoryPort,
 * so the domain never depends on a concrete store.
 */
@Service
public class OperationRepositoryAdapter implements OperationRepositoryPort {

    private final Map<Integer, Operation> store = new HashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    @Override
    public Operation save(Operation operation) {
        if (operation.getOperationId() == null) {
            operation.setOperationId(sequence.incrementAndGet());
        }
        store.put(operation.getOperationId(), operation);
        return operation;
    }

    @Override
    public Optional<Operation> findById(Operation operation) {
        if (operation == null || operation.getOperationId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(operation.getOperationId()));
    }

    @Override
    public List<Operation> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        return store.values().stream()
                .filter(op -> op.getPerformedBy() != null
                        && user.getUserId().equals(op.getPerformedBy().getUserId()))
                .toList();
    }

    @Override
    public List<Operation> findByProduct(BankingProduct product) {
        if (product == null || product.getIdentifier() == null) {
            return List.of();
        }
        return store.values().stream()
                .filter(op -> op.getAffectedProduct() != null
                        && product.getIdentifier().equals(op.getAffectedProduct().getIdentifier()))
                .toList();
    }

    @Override
    public List<Operation> findByType(Operation operation) {
        if (operation == null || operation.getOperationType() == null) {
            return List.of();
        }
        return store.values().stream()
                .filter(op -> operation.getOperationType().equals(op.getOperationType()))
                .toList();
    }
}
