package application.domain.models;

import application.domain.exceptions.InvalidCustomerException;
import java.time.LocalDate;
import java.time.Period;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NaturalCustomer extends Customer {
    private LocalDate birthDate;

    /**
     * Registers the natural customer after validating the minimum-age invariant.
     *
     * <p>A natural customer must be at least 18 years old (Domain Model.md -
     * NaturalCustomer). The service must not obtain the customer's age from the
     * database; the rule belongs to the Domain Model.
     */
    @Override
    public void register() {
        if (!isAdult()) {
            throw new InvalidCustomerException("Natural customer must be at least 18 years old.");
        }
        super.register();
    }

    public boolean isAdult() {
        if (birthDate == null) {
            return false;
        }
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}
