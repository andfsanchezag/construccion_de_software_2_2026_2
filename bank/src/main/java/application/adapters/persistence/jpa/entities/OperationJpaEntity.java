package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Operation aggregate.
 *
 * <p>Operation.affectedProduct is a polymorphic BankingProduct reference in the domain;
 * it is flattened here into an identifier plus a product type discriminator so the
 * correct domain type can be rebuilt when reading.
 */
@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
public class OperationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Integer operationId;

    /** OperationType catalog code. */
    @Column(name = "operation_type", nullable = false, length = 40)
    private String operationType;

    @Column(name = "execution_date")
    private LocalDateTime executionDate;

    /** Operation.performedBy reference. */
    @Column(name = "performed_by_user_id")
    private Integer performedByUserId;

    /** Operation.affectedProduct reference (polymorphic). */
    @Column(name = "affected_product_identifier", length = 60)
    private String affectedProductIdentifier;

    /** Reference type: BANK_ACCOUNT, LOAN or TRANSFER. */
    @Column(name = "affected_product_type", length = 20)
    private String affectedProductType;
}
