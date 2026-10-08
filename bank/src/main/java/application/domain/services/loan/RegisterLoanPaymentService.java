package application.domain.services.loan;

import application.domain.exceptions.CurrencyMismatchException;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanOperationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registra un pago contra un préstamo desembolsado.
 *
 * <p>Debita la cuenta origen (propia del solicitante, con saldo y misma moneda)
 * y registra la operación LOAN_PAYMENT. El préstamo debe estar en DISBURSED u
 * OVERDUE; de lo contrario se rechaza con 409.
 */
@Service
@RequiredArgsConstructor
public class RegisterLoanPaymentService {

    private static final Logger log = LoggerFactory.getLogger(RegisterLoanPaymentService.class);

    private final LoanRepositoryPort loanRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final AuthorizeLoanOperationService authorizeLoanOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan registerPayment(User user, Loan loan, BankAccount sourceAccount, Money amount) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
        if (loan == null || loan.getIdentifier() == null || loan.getIdentifier().isBlank()) {
            throw new EntityNotFoundException("Loan", loan != null ? loan.getIdentifier() : null);
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("Payment amount must be greater than zero.");
        }
        Loan stored = loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> {
                    log.warn("Pago de préstamo rechazado: préstamo no encontrado loanId='{}'.", loan.getIdentifier());
                    return new EntityNotFoundException("Loan", loan.getIdentifier());
                });
        authorizeLoanOperationService.execute(user, stored);
        if (!LoanStatus.DISBURSED.equals(stored.getLoanStatus())
                && !LoanStatus.OVERDUE.equals(stored.getLoanStatus())) {
            log.warn("Pago de préstamo rechazado: loanId='{}' status='{}' no admite pagos (requiere DISBURSED/OVERDUE).",
                    stored.getIdentifier(),
                    stored.getLoanStatus() != null ? stored.getLoanStatus().getCode() : "UNKNOWN");
            throw new DomainException("Loan payments are only allowed for DISBURSED or OVERDUE loans.");
        }
        BankAccount source = resolveSourceAccount(stored, sourceAccount, user);
        if (!source.getCurrency().equals(amount.getCurrency())) {
            // Si el cliente no envió currency, ajustamos a la moneda de la cuenta en lugar de fallar.
            amount = Money.of(amount.getAmount(), source.getCurrency());
        }
        if (!stored.getCurrency().equals(source.getCurrency())) {
            throw new CurrencyMismatchException("Loan currency and source account currency must match.");
        }
        source.withdraw(amount);
        bankAccountRepositoryPort.update(source);
        log.info("Pago de préstamo registrado: loanId='{}' amount='{} {}' desde cuenta='{}' por='{}'.",
                stored.getIdentifier(), amount.getAmount(), amount.getCurrency().getCode(),
                source.getIdentifier(), user.getUsername());
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_PAYMENT);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("amount", amount.getAmount());
        details.put("currency", amount.getCurrency().getCode());
        details.put("sourceAccount", source.getIdentifier());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }

    private BankAccount resolveSourceAccount(Loan stored, BankAccount sourceAccount, User user) {
        if (sourceAccount == null || sourceAccount.getIdentifier() == null || sourceAccount.getIdentifier().isBlank()) {
            // Sin cuenta origen explícita: usar la cuenta destino del préstamo si pertenece al solicitante.
            if (stored.getDestinationAccount() == null
                    || stored.getDestinationAccount().getIdentifier() == null) {
                throw new DomainException("Source account must be provided for loan payment.");
            }
            sourceAccount = stored.getDestinationAccount();
        }
        Optional<BankAccount> storedOpt = bankAccountRepositoryPort.findByIdentifier(sourceAccount);
        if (storedOpt.isEmpty()) {
            log.warn("Pago de préstamo rechazado: cuenta origen no encontrada sourceAccount='{}'.",
                    sourceAccount.getIdentifier());
            throw new EntityNotFoundException("Source account", sourceAccount.getIdentifier());
        }
        BankAccount source = storedOpt.get();
        if (source.getOwner() == null || stored.getApplicant() == null
                || !stored.getApplicant().getIdentification().equals(source.getOwner().getIdentification())) {
            throw new UnauthorizedOperationException("Source account must belong to the loan applicant.");
        }
        return source;
    }
}
