package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.TransferJpaEntity;
import application.adapters.persistence.jpa.mappers.BankAccountJpaMapper;
import application.adapters.persistence.jpa.mappers.TransferJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBankAccountRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaTransferRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.models.BankAccount;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.valueobjects.TransferStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Transfer backed by Spring Data JPA (MySQL).
 */
@Repository
public class TransferJpaAdapter implements TransferRepositoryPort {

    private static final List<String> PENDING_APPROVAL_STATUSES = List.of(
            TransferStatus.PENDING.getCode(),
            TransferStatus.WAITING_FOR_APPROVAL.getCode());

    private final SpringDataJpaTransferRepository transferRepository;
    private final SpringDataJpaBankAccountRepository bankAccountRepository;
    private final SpringDataJpaUserRepository userRepository;

    public TransferJpaAdapter(SpringDataJpaTransferRepository transferRepository,
                              SpringDataJpaBankAccountRepository bankAccountRepository,
                              SpringDataJpaUserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Transfer save(Transfer transfer) {
        if (transfer.getIdentifier() == null || transfer.getIdentifier().isBlank()) {
            transfer.setIdentifier("TR-" + UUID.randomUUID());
        }
        TransferJpaEntity saved = transferRepository.save(TransferJpaMapper.toEntity(transfer));
        return toDomain(saved);
    }

    @Override
    public Optional<Transfer> findByIdentifier(Transfer transfer) {
        if (transfer == null || transfer.getIdentifier() == null) {
            return Optional.empty();
        }
        return transferRepository.findById(transfer.getIdentifier()).map(this::toDomain);
    }

    @Override
    public List<Transfer> findBySourceAccount(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return List.of();
        }
        return transferRepository.findBySourceAccountIdentifier(account.getIdentifier()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> findByDestinationAccount(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return List.of();
        }
        return transferRepository.findByDestinationAccountIdentifier(account.getIdentifier()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> findPendingApproval() {
        return transferRepository.findByTransferStatusIn(PENDING_APPROVAL_STATUSES).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> findExpiredCandidates() {
        return transferRepository.findByTransferStatus(TransferStatus.WAITING_FOR_APPROVAL.getCode()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void update(Transfer transfer) {
        if (transfer == null || transfer.getIdentifier() == null) {
            return;
        }
        transferRepository.save(TransferJpaMapper.toEntity(transfer));
    }

    private Transfer toDomain(TransferJpaEntity entity) {
        BankAccount source = resolveAccount(entity.getSourceAccountIdentifier());
        BankAccount destination = resolveAccount(entity.getDestinationAccountIdentifier());
        User createdBy = resolveUser(entity.getCreatedByUserId());
        User approvedBy = resolveUser(entity.getApprovedByUserId());
        return TransferJpaMapper.toDomain(entity, source, destination, createdBy, approvedBy);
    }

    /**
     * Referenced accounts are rebuilt as identity references: transfer flows only need the
     * account identifier, and the owning customer is loaded by the BankAccount adapter
     * when the account itself is consulted.
     */
    private BankAccount resolveAccount(String identifier) {
        if (identifier == null) {
            return null;
        }
        return bankAccountRepository.findById(identifier)
                .map(accountEntity -> BankAccountJpaMapper.toDomain(accountEntity, null))
                .orElse(null);
    }

    private User resolveUser(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).map(entity -> UserJpaMapper.toDomain(entity, null)).orElse(null);
    }
}
