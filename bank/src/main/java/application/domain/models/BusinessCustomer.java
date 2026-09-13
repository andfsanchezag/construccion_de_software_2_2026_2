package application.domain.models;

import application.domain.exceptions.InvalidLegalRepresentativeException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusinessCustomer extends Customer {
    private NaturalCustomer legalRepresentative;

    /**
     * Registers the business customer after validating that it carries its domain
     * relationship with a legal representative.
     *
     * <p>The legal representative is a {@code NaturalCustomer} domain relationship
     * and must be provided before the business customer can be registered. Whether
     * the referenced representative exists in persistence and satisfies the
     * required state is an external validation performed by the service through
     * the CustomerRepositoryPort (customer-services.md 8.7 / 8.8).
     */
    @Override
    public void register() {
        if (!hasLegalRepresentative()) {
            throw new InvalidLegalRepresentativeException(
                    "A legal representative is required for business customer registration.");
        }
        super.register();
    }

    public boolean hasLegalRepresentative() {
        return legalRepresentative != null
                && legalRepresentative.getIdentification() != null
                && !legalRepresentative.getIdentification().isBlank();
    }
}
