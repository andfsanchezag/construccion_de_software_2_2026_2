package application.domain.services.operation;

import application.domain.models.Operation;
import application.domain.ports.out.OperationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterOperationService {

    private final OperationRepositoryPort operationRepositoryPort;

    public Operation execute(Operation operation) {
        return operationRepositoryPort.save(operation);
    }
}
