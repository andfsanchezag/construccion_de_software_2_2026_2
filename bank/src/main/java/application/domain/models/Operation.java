package application.domain.models;

import application.domain.valueobjects.OperationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Operation {
    private Integer operationId;
    private OperationType operationType;
    private LocalDateTime executionDate;
    private User performedBy;
    private BankingProduct affectedProduct;
}
