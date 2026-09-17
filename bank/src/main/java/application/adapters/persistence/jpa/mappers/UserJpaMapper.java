package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.UserStatus;

/**
 * Bidirectional mapper between the User Domain Model and its JPA entity.
 *
 * <p>The optional User.customer association is stored as a reference identifier; the
 * resolved Customer Domain Model is supplied by the adapter when reading.
 */
public final class UserJpaMapper {

    private UserJpaMapper() {
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUserId(domain.getUserId());
        entity.setUsername(domain.getUsername());
        entity.setPassword(domain.getPassword());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);
        entity.setRole(domain.getRole() != null ? domain.getRole().getCode() : null);
        entity.setIdentification(domain.getIdentification());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setAddress(domain.getAddress());
        entity.setCustomerIdentification(
                domain.getCustomer() != null ? domain.getCustomer().getIdentification() : null);
        return entity;
    }

    public static User toDomain(UserJpaEntity entity, Customer customer) {
        if (entity == null) {
            return null;
        }
        User domain = new User();
        domain.setUserId(entity.getUserId());
        domain.setUsername(entity.getUsername());
        domain.setPassword(entity.getPassword());
        domain.setStatus(userStatus(entity.getStatus()));
        domain.setRole(CustomerJpaMapper.systemRole(entity.getRole()));
        domain.setIdentification(entity.getIdentification());
        domain.setName(entity.getName());
        domain.setEmail(entity.getEmail());
        domain.setPhoneNumber(entity.getPhoneNumber());
        domain.setAddress(entity.getAddress());
        domain.setCustomer(customer);
        return domain;
    }

    public static UserStatus userStatus(String code) {
        if (code == null) {
            return null;
        }
        if (UserStatus.ACTIVE.getCode().equals(code)) {
            return UserStatus.ACTIVE;
        }
        if (UserStatus.INACTIVE.getCode().equals(code)) {
            return UserStatus.INACTIVE;
        }
        if (UserStatus.BLOCKED.getCode().equals(code)) {
            return UserStatus.BLOCKED;
        }
        return null;
    }
}
