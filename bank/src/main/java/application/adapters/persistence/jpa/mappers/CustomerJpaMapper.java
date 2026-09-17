package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.CustomerJpaEntity;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.SystemRole;

/**
 * Bidirectional mapper between the Customer Domain Model and its JPA entity.
 *
 * <p>The persisted Customer aggregate is flattened: the natural/business distinction is
 * kept as a discriminator value and the Catalog Value Objects are converted to/from
 * their domain codes. The domain model is never used as a database entity.
 */
public final class CustomerJpaMapper {

    public static final String TYPE_NATURAL = "NATURAL";
    public static final String TYPE_BUSINESS = "BUSINESS";

    private CustomerJpaMapper() {
    }

    public static CustomerJpaEntity toEntity(Customer domain) {
        if (domain == null) {
            return null;
        }
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setIdentification(domain.getIdentification());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setAddress(domain.getAddress());
        entity.setRole(domain.getRole() != null ? domain.getRole().getCode() : null);
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);

        if (domain instanceof BusinessCustomer businessCustomer) {
            entity.setCustomerType(TYPE_BUSINESS);
            entity.setLegalRepresentativeIdentification(
                    businessCustomer.getLegalRepresentative() != null
                            ? businessCustomer.getLegalRepresentative().getIdentification()
                            : null);
        } else {
            entity.setCustomerType(TYPE_NATURAL);
            if (domain instanceof NaturalCustomer naturalCustomer) {
                entity.setBirthDate(naturalCustomer.getBirthDate());
            }
        }
        return entity;
    }

    public static Customer toDomain(CustomerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Customer domain;
        if (TYPE_BUSINESS.equals(entity.getCustomerType())) {
            BusinessCustomer businessCustomer = new BusinessCustomer();
            if (entity.getLegalRepresentativeIdentification() != null) {
                NaturalCustomer representative = new NaturalCustomer();
                representative.setIdentification(entity.getLegalRepresentativeIdentification());
                businessCustomer.setLegalRepresentative(representative);
            }
            domain = businessCustomer;
        } else {
            NaturalCustomer naturalCustomer = new NaturalCustomer();
            naturalCustomer.setBirthDate(entity.getBirthDate());
            domain = naturalCustomer;
        }

        domain.setIdentification(entity.getIdentification());
        domain.setName(entity.getName());
        domain.setEmail(entity.getEmail());
        domain.setPhoneNumber(entity.getPhoneNumber());
        domain.setAddress(entity.getAddress());
        domain.setRole(systemRole(entity.getRole()));
        domain.setStatus(customerStatus(entity.getStatus()));
        return domain;
    }

    public static SystemRole systemRole(String code) {
        if (code == null) {
            return null;
        }
        if (SystemRole.NATURAL_CUSTOMER.getCode().equals(code)) {
            return SystemRole.NATURAL_CUSTOMER;
        }
        if (SystemRole.BUSINESS_CUSTOMER.getCode().equals(code)) {
            return SystemRole.BUSINESS_CUSTOMER;
        }
        if (SystemRole.TELLER_EMPLOYEE.getCode().equals(code)) {
            return SystemRole.TELLER_EMPLOYEE;
        }
        if (SystemRole.COMMERCIAL_EMPLOYEE.getCode().equals(code)) {
            return SystemRole.COMMERCIAL_EMPLOYEE;
        }
        if (SystemRole.BUSINESS_OPERATOR.getCode().equals(code)) {
            return SystemRole.BUSINESS_OPERATOR;
        }
        if (SystemRole.BUSINESS_SUPERVISOR.getCode().equals(code)) {
            return SystemRole.BUSINESS_SUPERVISOR;
        }
        if (SystemRole.INTERNAL_ANALYST.getCode().equals(code)) {
            return SystemRole.INTERNAL_ANALYST;
        }
        return null;
    }

    public static CustomerStatus customerStatus(String code) {
        if (code == null) {
            return null;
        }
        if (CustomerStatus.ACTIVE.getCode().equals(code)) {
            return CustomerStatus.ACTIVE;
        }
        if (CustomerStatus.INACTIVE.getCode().equals(code)) {
            return CustomerStatus.INACTIVE;
        }
        if (CustomerStatus.BLOCKED.getCode().equals(code)) {
            return CustomerStatus.BLOCKED;
        }
        return null;
    }
}
