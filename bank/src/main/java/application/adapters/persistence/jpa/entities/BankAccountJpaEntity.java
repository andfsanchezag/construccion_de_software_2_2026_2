package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the BankAccount aggregate.
 *
 * <p>The owner is stored as a reference identifier (BankAccount.owner is a Customer
 * aggregate) and the Value Objects are stored by their catalog code, keeping the
 * domain model completely out of the ORM mapping.
 */
@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
public class BankAccountJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    /** AccountType catalog code. */
    @Column(name = "account_type", length = 40)
    private String accountType;

    /** BankAccount.owner reference. */
    @Column(name = "owner_identification", nullable = false, length = 60)
    private String ownerIdentification;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private BigDecimal currentBalance;

    /** Currency catalog code. */
    @Column(name = "currency", length = 10)
    private String currency;

    /** AccountStatus catalog code. */
    @Column(name = "account_status", length = 40)
    private String accountStatus;

    @Column(name = "opening_date")
    private LocalDate openingDate;
}
