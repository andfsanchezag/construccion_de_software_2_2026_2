package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.adapters.persistence.jpa.mappers.CustomerJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCustomerRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for User backed by Spring Data JPA (MySQL).
 */
@Repository
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataJpaUserRepository userRepository;
    private final SpringDataJpaCustomerRepository customerRepository;

    public UserJpaAdapter(SpringDataJpaUserRepository userRepository,
                          SpringDataJpaCustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            return null;
        }
        return toDomain(userRepository.save(UserJpaMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findByUsername(User user) {
        if (user == null || user.getUsername() == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(user.getUsername()).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(User user) {
        if (user == null || user.getUserId() == null) {
            return Optional.empty();
        }
        return userRepository.findById(user.getUserId()).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(User user) {
        return user != null && user.getUsername() != null
                && userRepository.existsByUsername(user.getUsername());
    }

    @Override
    public void update(User user) {
        if (user == null) {
            return;
        }
        userRepository.save(UserJpaMapper.toEntity(user));
    }

    private User toDomain(UserJpaEntity entity) {
        Customer customer = entity.getCustomerIdentification() == null
                ? null
                : customerRepository.findById(entity.getCustomerIdentification())
                        .map(CustomerJpaMapper::toDomain)
                        .orElse(null);
        return UserJpaMapper.toDomain(entity, customer);
    }
}
