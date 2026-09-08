package application.domain.services.account;

import application.adapters.persistence.mongodb.AuditLogRepositoryAdapter;
import application.adapters.persistence.sql.BankAccountRepositoryAdapter;
import application.adapters.persistence.sql.CustomerRepositoryAdapter;
import application.adapters.persistence.sql.OperationRepositoryAdapter;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;

/**
 * Wires in-memory fakes of the output ports used by the Bank Account services.
 */
final class Harness {

    final BankAccountRepositoryAdapter bankAccountRepository = new BankAccountRepositoryAdapter();
    final CustomerRepositoryAdapter customerRepository = new CustomerRepositoryAdapter();
    final OperationRepositoryAdapter operationRepository = new OperationRepositoryAdapter();
    final AuditLogRepositoryAdapter auditLogRepository = new AuditLogRepositoryAdapter();
    final ValidateUserAuthorizationStatusService validateUserStatus = new ValidateUserAuthorizationStatusService();
    final RegisterOperationAndAuditService registerOperationAndAudit;

    Harness() {
        this.registerOperationAndAudit = new RegisterOperationAndAuditService(
                new RegisterOperationService(operationRepository),
                new RegisterAuditLogService(auditLogRepository));
    }
}