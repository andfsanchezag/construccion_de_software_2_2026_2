package application.domain.services.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import application.domain.models.BankAccount;
import application.domain.models.Transfer;
import application.domain.ports.out.TransferRepositoryPort;

final class InMemoryTransferRepository implements TransferRepositoryPort {

    private final List<Transfer> store = new ArrayList<>();

    void seed(Transfer transfer) {
        store.add(transfer);
    }

    @Override
    public Transfer save(Transfer transfer) {
        store.add(transfer);
        return transfer;
    }

    @Override
    public Optional<Transfer> findByIdentifier(Transfer transfer) {
        if (transfer == null || transfer.getIdentifier() == null) {
            return Optional.empty();
        }
        return store.stream()
                .filter(stored -> transfer.getIdentifier().equals(stored.getIdentifier()))
                .findFirst();
    }

    @Override
    public List<Transfer> findBySourceAccount(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return List.of();
        }
        return store.stream()
                .filter(transfer -> transfer.getSourceAccount() != null
                        && account.getIdentifier().equals(transfer.getSourceAccount().getIdentifier()))
                .toList();
    }

    @Override
    public List<Transfer> findByDestinationAccount(BankAccount account) {
        if (account == null || account.getIdentifier() == null) {
            return List.of();
        }
        return store.stream()
                .filter(transfer -> transfer.getDestinationAccount() != null
                        && account.getIdentifier().equals(transfer.getDestinationAccount().getIdentifier()))
                .toList();
    }

    @Override
    public List<Transfer> findPendingApproval() {
        return List.of();
    }

    @Override
    public List<Transfer> findExpiredCandidates() {
        return List.of();
    }

    @Override
    public void update(Transfer transfer) {
    }
}