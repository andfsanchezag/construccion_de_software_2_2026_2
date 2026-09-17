package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Transfer aggregate.
 *
 * <p>Source/destination accounts and the actors are stored as identifier references to
 * keep the ORM mapping decoupled from the domain aggregate graph.
 */
@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class TransferJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    /** Transfer.sourceAccount reference. */
    @Column(name = "source_account_identifier", nullable = false, length = 60)
    private String sourceAccountIdentifier;

    /** Transfer.destinationAccount reference. */
    @Column(name = "destination_account_identifier", nullable = false, length = 60)
    private String destinationAccountIdentifier;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    /** TransferStatus catalog code. */
    @Column(name = "transfer_status", length = 40)
    private String transferStatus;

    /** Transfer.createdBy reference. */
    @Column(name = "created_by_user_id")
    private Integer createdByUserId;

    /** Transfer.approvedBy reference. */
    @Column(name = "approved_by_user_id")
    private Integer approvedByUserId;
}
