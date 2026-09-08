package application.adapters.persistence.sql;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.ports.out.LoanRepositoryPort;

@Service
public class LoanRepositoryAdapter implements LoanRepositoryPort {

    @Override
    public Loan save(Loan loan) {
        // TODO Auto-generated method stub
        return loan;    
    }

    @Override
    public Optional<Loan> findByIdentifier(Loan loan) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByIdentifier'");
    }

    @Override
    public List<Loan> findByApplicant(Customer customer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByApplicant'");
    }

    @Override
    public List<Loan> findByStatus(Loan loan) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByStatus'");
    }

    @Override
    public void update(Loan loan) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
    
}
