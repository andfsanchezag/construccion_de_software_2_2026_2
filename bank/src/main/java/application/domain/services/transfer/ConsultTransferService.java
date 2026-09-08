package application.domain.services.transfer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultTransferService {

    private final TransferRepositoryPort transferRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public Transfer execute(User requestingUser, Transfer transfer) {
        if (transfer == null) {
            throw new EntityNotFoundException("Transfer");
        }
        // §12.4 requesting User is required when authorization is evaluated:
        // validate actor existence (no Operation/Audit: read-only §12.7).
        resolveRequestingUser(requestingUser);
        Optional<Transfer> found = transferRepositoryPort.findByIdentifier(transfer);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Transfer");
        }
        return found.get();
    }

    private void resolveRequestingUser(User requestingUser) {
        if (requestingUser == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        if (userRepositoryPort.findById(requestingUser).isEmpty()) {
            throw new UnauthorizedOperationException("Requesting user not found.");
        }
    }
}
