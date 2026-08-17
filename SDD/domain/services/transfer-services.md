# Transfer Services

## Introduction

This document defines the services belonging to the **Transfer Management** subdomain of the Banking Information Management System.

The services in this subdomain are responsible for creating, executing, approving, rejecting, and expiring fund transfers between bank accounts.

Transfers are banking products and services represented by the `Transfer` Domain Model. They inherit from `BankingProduct`.

```text
BankingProduct
      │
      └── Transfer
```

Every significant action in the transfer lifecycle generates an `Operation` and a corresponding `AuditLog` record.

The services described here define the conceptual behavior of the Transfer subdomain. REST contracts, persistence mappings, and infrastructure-specific concerns must be documented separately.

---

# Domain Model Context

A `Transfer` represents a service that moves funds between two bank accounts.

Conceptually:

```text
BankingProduct
      │
      └── Transfer
             │
             ├── sourceAccount : BankAccount
             ├── destinationAccount : BankAccount
             ├── amount : BigDecimal
             ├── creationDate : LocalDateTime
             ├── approvalDate : LocalDateTime
             ├── transferStatus : TransferStatus
             ├── createdBy : User
             └── approvedBy : User?
```

All relationships must use Domain Models.

The Domain Model must not replace these relationships with primitive identifiers such as:

```text
String sourceAccountId
String destinationAccountId
String createdByUserId
```

Persistence adapters are responsible for translating Domain relationships into their database representation.

---

# Service Design Principles

## Domain Model Parameters

All Transfer services and Input Ports must receive Domain Models or Value Objects.

They must never receive:

* Primitive identifiers.
* `String` identifiers.
* Individual attributes as substitutes for Domain Models.
* REST Request DTOs.
* Persistence entities.

### Incorrect

```java
createTransfer(
    String sourceAccountId,
    String destinationAccountId,
    BigDecimal amount
);
```

### Correct

```java
createTransfer(Transfer transfer);
```

The `Transfer` Domain Model must contain all information required for the operation.

---

# External Information

Transfer business rules must first use information available in the Domain Model.

If a rule requires information not available in the supplied Domain Model, the service must obtain it through an Output Port.

The service must never access:

* MySQL.
* MongoDB.
* JPA.
* SQL.
* REST clients.
* External infrastructure.

directly.

The communication flow is:

```text
Transfer Service
      │
      ▼
Output Port
      │
      ▼
Output Adapter
      │
      ▼
External Resource
```

---

# Transfer Lifecycle

The Transfer lifecycle is represented through `TransferStatus`.

```text
PENDING
   │
   ├──────────────► REJECTED
   │
   ▼
WAITING_FOR_APPROVAL
   │
   ├──────────────► REJECTED
   │
   ├──────────────► EXPIRED
   │
   ▼
APPROVED
   │
   ▼
EXECUTED
```

Depending on the transfer amount and the applicable business rules, a transfer may either proceed directly to `APPROVED` or enter `WAITING_FOR_APPROVAL`.

The Domain is responsible for validating all status transitions.

---

# Approval Threshold

The system determines whether a transfer requires approval based on a configurable threshold.

Because the threshold is not a fixed business rule hardcoded in the domain, it must be obtained through an Output Port.

```text
Transfer Service
      │
      ▼
TransferConfigurationPort
      │
      ▼
Approval Threshold
      │
      ▼
Transfer amount > threshold?
      │
      ├── YES → WAITING_FOR_APPROVAL
      │
      └── NO  → APPROVED
```

The service must never hardcode a specific amount as the threshold.

---

# 1. Create Transfer

## Description

Creates a transfer request between two bank accounts and establishes its initial state.

Depending on the applicable business rules:

* If the transfer does not require approval, it enters `APPROVED`.
* If the transfer requires approval, it enters `WAITING_FOR_APPROVAL`.

---

## Input

```text
Transfer
```

The `Transfer` Domain Model must contain:

* Source account.
* Destination account.
* Amount.
* Creation date.
* Creating user.
* Initial transfer status.

---

## Domain Validations

The service validates:

* Source account is valid and operational.
* Source account status is `ACTIVE`.
* Destination account is valid.
* Destination account status is `ACTIVE`.
* Amount is positive.
* Source and destination accounts are not the same.
* The creating user is authorized to create the transfer.
* Sufficient balance in the source account when required.
* Currency compatibility when applicable.

If information required for any validation is not present in the Domain Models, the service uses the appropriate Output Port.

---

## Approval Threshold Check

After domain validation:

```text
Transfer.amount
      │
      ▼
TransferConfigurationPort
      │
      ▼
Requires approval?
      │
      ├── YES → TransferStatus = WAITING_FOR_APPROVAL
      │
      └── NO  → TransferStatus = APPROVED
```

---

## Persistence

After validation and status assignment:

```text
TransferRepository
```

persists the transfer.

---

## Operation and Audit

Creating a transfer is a significant business operation.

The operation type is:

```text
TRANSFER_CREATION
```

Conceptually:

```text
Transfer
    │
    ▼
Create Transfer
    │
    ├── Persist Transfer
    │
    └── Operation
           │
           ▼
        AuditLog
```

---

# 2. Execute Transfer

## Description

Executes a transfer that has been approved and is ready for execution.

The service moves funds from the source account to the destination account.

---

## Input

```text
User
Transfer
```

---

## Authorization

The executing user must be authorized to execute the transfer.

The authorization depends on the user's role and the state of the transfer.

---

## Domain Validations

The service validates:

* Transfer status is `APPROVED`.
* Source account status is `ACTIVE`.
* Destination account status is `ACTIVE`.
* Sufficient balance in the source account.
* The `APPROVED → EXECUTED` transition is valid.

---

## Balance Modification

Conceptually:

```text
Transfer
    │
    ├── Debit sourceAccount.currentBalance
    │
    └── Credit destinationAccount.currentBalance
```

The service must not manipulate database fields directly.

The balance modification must be performed through the Domain Model behavior.

---

## Persistence

```text
TransferRepository
BankAccountRepository (source)
BankAccountRepository (destination)
```

---

## Operation and Audit

The operation type is:

```text
TRANSFER_EXECUTION
```

The audit details should record:

```text
amount
balanceBeforeOrigin
balanceAfterOrigin
balanceBeforeDestination
balanceAfterDestination
```

---

# 3. Submit Transfer for Approval

## Description

Places a transfer into the `WAITING_FOR_APPROVAL` state when the applicable business rules require authorization before execution.

This transition may be applied when a transfer is created with an amount exceeding the configured approval threshold.

---

## Input

```text
Transfer
```

---

## Domain Validations

The service validates:

* Transfer status allows the transition to `WAITING_FOR_APPROVAL`.
* The transfer requires approval according to the business rules.

---

## Persistence

```text
TransferRepository
```

---

## Operation and Audit

This transition is part of the transfer creation flow and is covered by the `TRANSFER_CREATION` operation when the approval requirement is determined at creation time.

If treated as a separate lifecycle event, a dedicated operation may be registered at the domain's discretion.

---

# 4. Approve Transfer

## Description

Approves a transfer that requires authorization and allows it to proceed to execution.

---

## Input

```text
User
Transfer
```

---

## Authorization

The approving user must have the appropriate role.

For transfers initiated by business customers, the approving user must have the:

```text
BUSINESS_SUPERVISOR
```

role.

The authorization rule is:

```text
User.role == BUSINESS_SUPERVISOR
    AND
User.status == ACTIVE
    AND
Transfer.transferStatus == WAITING_FOR_APPROVAL
```

If additional authorization information is required, the service must use an Output Port.

---

## Domain Validations

The service validates:

* Transfer status is `WAITING_FOR_APPROVAL`.
* The approving user is not the same user who created the transfer when applicable.
* The `WAITING_FOR_APPROVAL → APPROVED` transition is valid.

---

## Approval Date

The approval date must be set in the `Transfer` Domain Model:

```text
Transfer.approvalDate
```

The approving user must be recorded:

```text
Transfer.approvedBy : User
```

---

## Persistence

```text
TransferRepository
```

---

## Operation and Audit

The operation type is:

```text
TRANSFER_APPROVAL
```

The audit details should record:

```text
previousStatus
newStatus
approvedBy
approvalDate
```

---

# 5. Reject Transfer

## Description

Rejects a transfer that is awaiting approval.

---

## Input

```text
User
Transfer
```

---

## Authorization

The rejecting user must have the appropriate authorization role.

The applicable roles follow the same rule as for transfer approval.

---

## Domain Validations

The service validates:

* Transfer status is `WAITING_FOR_APPROVAL` or `PENDING`.
* The rejection transition is valid from the current state.

---

## Persistence

```text
TransferRepository
```

---

## Operation and Audit

The operation type is:

```text
TRANSFER_REJECTION
```

The audit details should record:

```text
previousStatus
newStatus
rejectedBy
rejectionDate
```

---

# 6. Expire Transfer

## Description

Marks a transfer as expired when it remains in `WAITING_FOR_APPROVAL` beyond the permitted approval period.

---

## Input

```text
Transfer
```

---

## Domain Validations

The service validates:

* Transfer status is `WAITING_FOR_APPROVAL`.
* The permitted approval time window has elapsed.
* The `WAITING_FOR_APPROVAL → EXPIRED` transition is valid.

---

## Expiration Check

The service must determine whether the approval time window has expired.

Because the allowed approval period may be configurable, the service may obtain it through:

```text
TransferConfigurationPort
```

---

## Balance Impact

Expiration must **not** move funds.

The source account balance must not be modified by expiration.

---

## Persistence

```text
TransferRepository
```

---

## Operation and Audit

The operation type is:

```text
TRANSFER_EXPIRATION
```

The audit details should record:

```text
reason
expirationDate
```

---

# 7. Consult Transfer

## Description

Retrieves a transfer represented by the `Transfer` Domain Model.

The result must not expose persistence entities.

---

## Input

```text
Transfer
```

---

## Processing

```text
Transfer
    │
    ▼
TransferRepository
    │
    ▼
Transfer
```

---

## Authorization

The service must verify that the requesting user has permission to consult the transfer.

Authorization may depend on:

* User role.
* Relationship between the user and the accounts involved in the transfer.

---

# Output Ports

The Transfer subdomain requires the following Output Ports:

```text
TransferRepository
BankAccountRepository
TransferConfigurationPort
OperationRepository
AuditRepository
```

---

## TransferRepository

Provides the persistence capabilities required by Transfer Services.

Conceptually:

```java
interface TransferRepository {

    Transfer save(Transfer transfer);

    Transfer find(Transfer transfer);

    boolean exists(Transfer transfer);
}
```

The interface belongs to:

```text
domain/ports/out/
```

---

## TransferConfigurationPort

Provides business configuration values required by Transfer Services, such as the approval threshold.

Conceptually:

```java
interface TransferConfigurationPort {

    BigDecimal getApprovalThreshold();

    Duration getApprovalExpirationPeriod();
}
```

The Domain must not hardcode these values.

The adapter is responsible for retrieving configuration from the appropriate infrastructure source.

---

## BankAccountRepository

Provides access to `BankAccount` Domain Models when Transfer Services require account information.

---

## OperationRepository

Persists business operations generated by the Transfer lifecycle.

---

## AuditRepository

Persists audit records generated by Transfer business operations.

The adapter is responsible for the MongoDB implementation.

---

# Input Ports

The Transfer subdomain exposes the following conceptual use cases:

```text
CreateTransferUseCase
ExecuteTransferUseCase
ApproveTransferUseCase
RejectTransferUseCase
ExpireTransferUseCase
ConsultTransferUseCase
```

Each Input Port must follow the Domain Model parameter rule.

---

# Example Input Ports

```java
interface CreateTransferUseCase {

    Transfer create(Transfer transfer);
}
```

```java
interface ApproveTransferUseCase {

    Transfer approve(User user, Transfer transfer);
}
```

```java
interface RejectTransferUseCase {

    Transfer reject(User user, Transfer transfer);
}
```

```java
interface ExpireTransferUseCase {

    Transfer expire(Transfer transfer);
}
```

---

# Transfer Creation Flow

```text
HTTP Request
     │
     ▼
Request DTO
     │
     ▼
Request Mapper
     │
     ▼
Transfer Domain Model
     │
     ▼
CreateTransferUseCase
     │
     ▼
Transfer Service
     │
     ├── Validate Domain Rules
     │
     ├── BankAccountRepository
     │
     ├── TransferConfigurationPort
     │
     ├── TransferRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```

---

# Transfer Execution Flow

```text
Transfer (APPROVED)
     │
     ▼
ExecuteTransferUseCase
     │
     ▼
Transfer Service
     │
     ├── Validate Transfer Status
     │
     ├── Validate Source Account Balance
     │
     ├── Debit Source Account
     │
     ├── Credit Destination Account
     │
     ├── BankAccountRepository (source)
     │
     ├── BankAccountRepository (destination)
     │
     ├── TransferRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```
