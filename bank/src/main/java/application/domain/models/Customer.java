package application.domain.models;

import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.InvalidCustomerStatusException;
import application.domain.valueobjects.CustomerStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Customer extends Person {
    private CustomerStatus status;
    // Loaded on demand by ConsultCustomerProductsService; empty by default.
    private List<BankAccount> accounts = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();
    private List<Transfer> transfers = new ArrayList<>();

    /**
     * Registers the customer establishing the initial {@code ACTIVE} status.
     *
     * <p>The initial lifecycle state is decided by the Domain Model; callers must
     * not assign {@code status} directly during registration. A customer that
     * already holds a status cannot be registered again.
     */
    public void register() {
        if (status != null) {
            throw new InvalidCustomerException(
                    "Customer is already registered with status " + statusCode() + ".");
        }
        this.status = CustomerStatus.ACTIVE;
    }

    /**
     * Changes the customer status enforcing the transition matrix owned by the
     * Customer Domain Model.
     *
     * <p>Supported transitions:
     * {@code ACTIVE -> BLOCKED}, {@code ACTIVE -> INACTIVE},
     * {@code BLOCKED -> ACTIVE}, {@code INACTIVE -> ACTIVE}.
     *
     * <p>CustomerStatus is independent from UserStatus: this operation never
     * changes the status of any associated system user.
     */
    public void changeStatus(CustomerStatus target) {
        if (target == null) {
            throw new InvalidCustomerStatusException("Target customer status must be provided.");
        }
        if (!isValidTransition(status, target)) {
            throw new InvalidCustomerStatusException(
                    "Invalid customer status transition from " + statusCode() + " to " + target.getCode() + ".");
        }
        this.status = target;
    }

    /**
     * Applies the mutable customer information supplied by the desired state.
     *
     * <p>Only the mutable contact information (name, email, phone and address) is
     * copied. Identification is an identity attribute and cannot be changed by a
     * generic update; {@code status} has its own dedicated use case and is never
     * mutated here. Domain invariants are enforced before any value is copied.
     */
    public void updateFrom(Customer desired) {
        if (desired == null) {
            throw new InvalidCustomerException("Desired customer state must be provided.");
        }
        if (desired.getIdentification() == null || !desired.getIdentification().equals(getIdentification())) {
            throw new InvalidCustomerException("Customer identification cannot be changed by update.");
        }
        requireNonBlank(desired.getName(), "name");
        requireNonBlank(desired.getEmail(), "email");
        requireNonBlank(desired.getPhoneNumber(), "phoneNumber");
        requireNonBlank(desired.getAddress(), "address");
        setName(desired.getName());
        setEmail(desired.getEmail());
        setPhoneNumber(desired.getPhoneNumber());
        setAddress(desired.getAddress());
    }

    /**
     * The customer status transition matrix (customer-services.md 11.6).
     */
    private static boolean isValidTransition(CustomerStatus current, CustomerStatus target) {
        return (CustomerStatus.ACTIVE.equals(current) && CustomerStatus.BLOCKED.equals(target))
                || (CustomerStatus.ACTIVE.equals(current) && CustomerStatus.INACTIVE.equals(target))
                || (CustomerStatus.BLOCKED.equals(current) && CustomerStatus.ACTIVE.equals(target))
                || (CustomerStatus.INACTIVE.equals(current) && CustomerStatus.ACTIVE.equals(target));
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidCustomerException("Customer " + field + " must be provided for update.");
        }
    }

    protected String statusCode() {
        return status == null ? "UNKNOWN" : status.getCode();
    }
}
