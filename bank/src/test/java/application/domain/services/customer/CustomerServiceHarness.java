package application.domain.services.customer;

import application.adapters.persistence.mongodb.AuditLogRepositoryAdapter;
import application.adapters.persistence.sql.BankAccountRepositoryAdapter;
import application.adapters.persistence.sql.CustomerRepositoryAdapter;
import application.adapters.persistence.sql.OperationRepositoryAdapter;
import application.domain.models.AuditLog;
import application.domain.models.Operation;
import application.domain.services.authorization.AuthorizeChangeCustomerStatusService;
import application.domain.services.authorization.AuthorizeCustomerOperationService;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;
import application.domain.valueobjects.OperationType;

final class CustomerServiceHarness {

    final CustomerRepositoryAdapter customerRepository = new CustomerRepositoryAdapter();
    final BankAccountRepositoryAdapter bankAccountRepository = new BankAccountRepositoryAdapter();
    final InMemoryLoanRepository loanRepository = new InMemoryLoanRepository();
    final InMemoryTransferRepository transferRepository = new InMemoryTransferRepository();
    final OperationRepositoryAdapter operationRepository = new OperationRepositoryAdapter();
    final AuditLogRepositoryAdapter auditLogRepository = new AuditLogRepositoryAdapter();
    final ValidateUserAuthorizationStatusService validateUserStatus = new ValidateUserAuthorizationStatusService();
    final RegisterOperationAndAuditService registerOperationAndAudit;
    final AuthorizeCustomerOperationService authorizeCustomerOperation;
    final AuthorizeCustomerRegistrationService authorizeCustomerRegistration;
    final AuthorizeChangeCustomerStatusService authorizeChangeCustomerStatus;

    CustomerServiceHarness() {
        this.registerOperationAndAudit = new RegisterOperationAndAuditService(
                new RegisterOperationService(operationRepository),
                new RegisterAuditLogService(auditLogRepository));
        this.authorizeCustomerOperation = new AuthorizeCustomerOperationService(validateUserStatus);
        this.authorizeCustomerRegistration = new AuthorizeCustomerRegistrationService(validateUserStatus);
        this.authorizeChangeCustomerStatus = new AuthorizeChangeCustomerStatusService(validateUserStatus);
    }

    int operationCount(OperationType type) {
        Operation template = new Operation();
        template.setOperationType(type);
        return operationRepository.findByType(template).size();
    }

    int auditCount(OperationType type) {
        AuditLog template = new AuditLog();
        template.setOperationType(type);
        return auditLogRepository.findByOperationType(template).size();
    }
}