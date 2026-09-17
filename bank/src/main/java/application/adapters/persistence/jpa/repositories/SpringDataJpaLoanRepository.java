package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.LoanJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Loan persistence entity.
 */
public interface SpringDataJpaLoanRepository extends JpaRepository<LoanJpaEntity, String> {

    List<LoanJpaEntity> findByApplicantIdentification(String applicantIdentification);

    List<LoanJpaEntity> findByLoanStatus(String loanStatus);
}
