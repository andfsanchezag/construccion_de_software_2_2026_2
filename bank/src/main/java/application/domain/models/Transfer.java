package application.domain.models;

import application.domain.valueobjects.TransferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Transfer extends BankingProduct {
    private BankAccount sourceAccount;
    private BankAccount destinationAccount;
    private BigDecimal amount;
    private LocalDateTime creationDate;
    private LocalDateTime approvalDate;
    private TransferStatus transferStatus;
    private User createdBy;
    private User approvedBy;
}
