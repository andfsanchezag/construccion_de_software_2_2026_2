package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeTransferApprovalService {

    private final ValidateBusinessSupervisorAuthorizationService validateBusinessSupervisorAuthorizationService;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final UserRepositoryPort userRepositoryPort;

    /**
     * Pure authorization validator for transfer approval.
     * Resolves the authoritative User, enforces segregation-of-duties and
     * delegates role/status checks. It never mutates, persists nor registers
     * operations: persistence/audit stays in ApproveTransferService (single
     * orchestration, no double execution).
     *
     * @return the authoritative stored User to be used as approver.
     */
    public User execute(User user, Transfer transfer) {
        if (user == null) {
            throw new UnauthorizedOperationException("User must be provided.");
        }
        if (transfer == null) {
            throw new UnauthorizedOperationException("Transfer must be provided.");
        }
        Optional<User> storedUserOpt = userRepositoryPort.findById(user);
        if (storedUserOpt.isEmpty()) {
            throw new UnauthorizedOperationException("User not found.");
        }
        User storedUser = storedUserOpt.get();

        validateSegregationOfDuties(storedUser, transfer);
        validateBusinessSupervisorAuthorizationService.execute(storedUser, transfer);

        return storedUser;
    }

    /**
     * Authorization for rejection: same actor rules as approval (active
     * BUSINESS_SUPERVISOR + segregation) but valid from PENDING or
     * WAITING_FOR_APPROVAL (§10.5), without imposing the WAITING-only guard
     * of the approval validator.
     */
    public User executeForRejection(User user, Transfer transfer) {
        if (user == null) {
            throw new UnauthorizedOperationException("User must be provided.");
        }
        if (transfer == null) {
            throw new UnauthorizedOperationException("Transfer must be provided.");
        }
        Optional<User> storedUserOpt = userRepositoryPort.findById(user);
        if (storedUserOpt.isEmpty()) {
            throw new UnauthorizedOperationException("User not found.");
        }
        User storedUser = storedUserOpt.get();

        validateSegregationOfDuties(storedUser, transfer);
        validateUserAuthorizationStatusService.execute(storedUser);
        if (!SystemRole.BUSINESS_SUPERVISOR.equals(storedUser.getRole())) {
            throw new UnauthorizedOperationException("BUSINESS_SUPERVISOR role is required for transfer rejection.");
        }
        if (!TransferStatus.PENDING.equals(transfer.getTransferStatus())
                && !TransferStatus.WAITING_FOR_APPROVAL.equals(transfer.getTransferStatus())) {
            String from = transfer.getTransferStatus() == null
                    ? "UNKNOWN"
                    : transfer.getTransferStatus().getCode();
            throw new UnauthorizedOperationException("Transfer cannot be rejected from status " + from + ".");
        }

        return storedUser;
    }

    private void validateSegregationOfDuties(User storedUser, Transfer transfer) {
        // Approver must not approve a transfer from their own account / created by themselves.
        if (transfer.getSourceAccount() != null
                && transfer.getSourceAccount().getOwner() != null
                && storedUser.getCustomer() != null
                && storedUser.getCustomer().getIdentification() != null
                && storedUser.getCustomer().getIdentification()
                        .equals(transfer.getSourceAccount().getOwner().getIdentification())) {
            throw new UnauthorizedOperationException("The user cannot approve a transfer from their own account.");
        }
        if (transfer.getCreatedBy() != null && isSameUser(storedUser, transfer.getCreatedBy())) {
            throw new UnauthorizedOperationException("Approving user must be different from creating user.");
        }
    }

    private boolean isSameUser(User a, User b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getUserId() != null && b.getUserId() != null) {
            return a.getUserId().equals(b.getUserId());
        }
        if (a.getIdentification() != null && b.getIdentification() != null) {
            return a.getIdentification().equals(b.getIdentification());
        }
        if (a.getUsername() != null && b.getUsername() != null) {
            return a.getUsername().equals(b.getUsername());
        }
        return false;
    }
}
