package application.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusinessCustomer extends Customer {
    private NaturalCustomer legalRepresentative;
}
