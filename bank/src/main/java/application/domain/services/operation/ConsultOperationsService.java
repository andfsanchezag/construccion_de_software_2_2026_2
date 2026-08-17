package application.domain.services.operation;

import application.domain.models.BankingProduct;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultOperationsService {

    private final OperationRepositoryPort operationRepositoryPort;

    public List<Operation> executeByUser(User requestingUser, User user) {
        return operationRepositoryPort.findByUser(user);
    }

    public List<Operation> executeByProduct(User requestingUser, BankingProduct product) {
        return operationRepositoryPort.findByProduct(product);
    }
}
