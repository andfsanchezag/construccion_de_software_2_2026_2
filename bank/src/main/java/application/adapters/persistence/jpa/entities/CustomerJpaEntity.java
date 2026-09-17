package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Customer aggregate.
 *
 * <p>It is intentionally independent from the domain model: the domain never
 * references this class. Inheritance is flattened into a {@code customer_type}
 * discriminator column plus the fields each subtype requires, so a single table
 * stores both natural and business customers.
 *
 * <p>Value Objects (SystemRole, CustomerStatus) are stored by their catalog code.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class CustomerJpaEntity {

    @Id
    @Column(name = "identification", nullable = false, length = 60)
    private String identification;

    /** Discriminator: NATURAL or BUSINESS. */
    @Column(name = "customer_type", nullable = false, length = 20)
    private String customerType;

    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    /** SystemRole catalog code. */
    @Column(name = "role", length = 40)
    private String role;

    /** CustomerStatus catalog code. */
    @Column(name = "status", length = 40)
    private String status;

    /** NaturalCustomer.birthDate. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** BusinessCustomer.legalRepresentative reference. */
    @Column(name = "legal_representative_identification", length = 60)
    private String legalRepresentativeIdentification;
}
