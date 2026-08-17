package application.domain.services.transfer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.TransferRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultTransferService {

    private final TransferRepositoryPort transferRepositoryPort;

    public Transfer execute(User requestingUser, Transfer transfer) {
        Optional<Transfer> found = transferRepositoryPort.findByIdentifier(transfer);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        return found.get();
    }
}
