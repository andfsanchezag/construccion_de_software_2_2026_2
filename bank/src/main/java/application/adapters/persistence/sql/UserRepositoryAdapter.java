package application.adapters.persistence.sql;

import java.util.Optional;

import org.springframework.stereotype.Service;

import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;

@Service
public class UserRepositoryAdapter implements UserRepositoryPort{

    @Override
    public User save(User user) {
        // TODO Auto-generated method stub
        return user;
    }

    @Override
    public Optional<User> findByUsername(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByUsername'");
    }

    @Override
    public Optional<User> findById(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public boolean existsByUsername(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsByUsername'");
    }

    @Override
    public void update(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
    
}
