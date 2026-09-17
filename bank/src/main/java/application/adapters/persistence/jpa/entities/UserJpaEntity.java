package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the User aggregate.
 *
 * <p>User extends Person in the domain, so its inherited attributes are flattened
 * here. The optional association with a Customer is stored as an identifier
 * reference; the adapter resolves the domain object when reading.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password", length = 200)
    private String password;

    /** UserStatus catalog code. */
    @Column(name = "status", length = 40)
    private String status;

    /** SystemRole catalog code (inherited from Person). */
    @Column(name = "role", length = 40)
    private String role;

    @Column(name = "identification", length = 60)
    private String identification;

    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    /** User.customer reference. */
    @Column(name = "customer_identification", length = 60)
    private String customerIdentification;
}
