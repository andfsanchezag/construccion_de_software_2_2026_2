package application.domain.ports.out;

import application.domain.models.BankAccount;
import application.domain.models.Transfer;

import java.util.List;
import java.util.Optional;

public interface TransferRepositoryPort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByIdentifier(Transfer transfer);

    List<Transfer> findBySourceAccount(BankAccount account);

    List<Transfer> findByDestinationAccount(BankAccount account);

    List<Transfer> findPendingApproval();

    List<Transfer> findExpiredCandidates();

    void update(Transfer transfer);
}
