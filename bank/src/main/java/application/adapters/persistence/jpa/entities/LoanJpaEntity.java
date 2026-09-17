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
 * JPA persistence entity (repository DTO) for the Loan aggregate.
 *
 * <p>Loan.applicant and Loan.destinationAccount are aggregate references, stored as
 * identifiers. The lifecycle fields (loanStatus, approvalDate, disbursementDate) are
 * persisted so the Loan domain transitions can be restored when reading.
 */
@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class LoanJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    /** Loan.applicant reference. */
    @Column(name = "applicant_identification", nullable = false, length = 60)
    private String applicantIdentification;

    /** LoanType catalog code. */
    @Column(name = "loan_type", length = 40)
    private String loanType;

    @Column(name = "requested_amount", precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "approved_amount", precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "interest_rate", precision = 9, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "term_in_months")
    private Integer termInMonths;

    /** LoanStatus catalog code. */
    @Column(name = "loan_status", length = 40)
    private String loanStatus;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    /** Loan.destinationAccount reference. */
    @Column(name = "destination_account_identifier", length = 60)
    private String destinationAccountIdentifier;

    /** Currency catalog code. */
    @Column(name = "currency", length = 10)
    private String currency;
}
