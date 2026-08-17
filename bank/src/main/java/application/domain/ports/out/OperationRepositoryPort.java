package application.domain.ports.out;

import application.domain.models.BankingProduct;
import application.domain.models.Operation;
import application.domain.models.User;

import java.util.List;
import java.util.Optional;

public interface OperationRepositoryPort {

    Operation save(Operation operation);

    Optional<Operation> findById(Operation operation);

    List<Operation> findByUser(User user);

    List<Operation> findByProduct(BankingProduct product);

    List<Operation> findByType(Operation operation);
}
