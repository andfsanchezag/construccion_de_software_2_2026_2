# Transfer Services

## 1. Purpose

This document defines the application services of the **Transfer
Management** subdomain of the Banking Information Management System.

The services manage the transfer lifecycle:

``` text
Create Transfer
      |
      +--> APPROVED ------------------> Execute Transfer
      |
      +--> WAITING_FOR_APPROVAL
                |
                +--> Approve Transfer --> Execute Transfer
                |
                +--> Reject Transfer
                |
                +--> Expire Transfer

Consult Transfer
```

`Transfer` is a `BankingProduct` Domain Model. Significant transfer
lifecycle actions generate an `Operation`, which is subsequently
registered in the audit trail through the Operation and Audit services.

This document describes Domain/Application behavior and its interaction
with Input Ports and Output Ports. REST DTOs, persistence entities,
controllers, JPA mappings, SQL, MongoDB, and infrastructure-specific
implementations remain outside this layer.

------------------------------------------------------------------------

# 2. Authoritative Design Rules

## 2.1 Domain-first

Transfer services operate on Domain Models and Value Objects.

They must not receive:

-   REST Request DTOs.
-   Persistence entities.
-   Primitive identifiers as substitutes for Domain Models.
-   Individual attributes when the corresponding Domain Model exists.

Correct:

``` java
Transfer execute(Transfer transfer, User user, Customer customer);
```

Incorrect:

``` java
Transfer execute(
    String sourceAccountId,
    String destinationAccountId,
    BigDecimal amount,
    Integer userId
);
```

The application/adapter layer is responsible for translating external
representations into Domain Models before invoking the Input Port.

## 2.2 Enrichment before business validation

When the supplied Domain Model does not contain authoritative
information required by the use case, the service resolves the
corresponding Domain Model through an Output Port.

The general pattern is:

``` text
Input Domain Models
        |
        v
Resolve authoritative entities
        |
        v
Enrich execution context
        |
        v
Validate relationships and business rules
        |
        v
Execute domain behavior
        |
        v
Persist
        |
        v
Register Operation + Audit
        |
        v
Return Domain Model
```

Enrichment does not mean replacing Domain Models with primitive
identifiers. Identifiers may be used internally by an adapter to locate
an entity, but the service works with the resulting Domain Model.

## 2.3 Output Ports

A Transfer service must never access infrastructure directly.

``` text
Transfer Service
       |
       v
Output Port
       |
       v
Output Adapter
       |
       v
External Resource
```

Typical Transfer dependencies are:

``` text
TransferRepositoryPort
BankAccountRepositoryPort
UserRepositoryPort
BusinessConfigurationPort
```

Additional authorization, notification, operation, or audit capabilities
are used through their respective domain ports/services when required by
the concrete implementation.

## 2.4 Single-service orchestration

Each service may contain the complete orchestration required for its use
case.

The objective is not to artificially fragment validations into many
services. The objective is to maintain:

-   high cohesion;
-   low coupling;
-   explicit business flow;
-   dependency inversion;
-   testability;
-   domain independence.

------------------------------------------------------------------------

# 3. Transfer Domain Context

Conceptually:

``` text
BankingProduct
      |
      +--> Transfer
             |
             +--> sourceAccount : BankAccount
             +--> destinationAccount : BankAccount
             +--> amount : BigDecimal
             +--> creationDate : LocalDateTime
             +--> approvalDate : LocalDateTime
             +--> transferStatus : TransferStatus
             +--> createdBy : User
             +--> approvedBy : User?
```

The Domain Model defines the relationships as objects:

``` text
Transfer
   |
   +--> BankAccount
   +--> BankAccount
   +--> User
   +--> User
```

The service may receive a `Transfer` whose account references are
sufficient to locate authoritative account models through
`BankAccountRepositoryPort`. The enrichment process then works with the
resolved `BankAccount` Domain Models.

------------------------------------------------------------------------

# 4. Transfer Lifecycle

The supported lifecycle is:

``` text
PENDING
   |
   +------------------> REJECTED
   |
   v
WAITING_FOR_APPROVAL
   |
   +------------------> REJECTED
   |
   +------------------> EXPIRED
   |
   v
APPROVED
   |
   v
EXECUTED
```

At creation, the current implementation determines the initial status
from the configured approval threshold:

``` text
amount > threshold
      |
      +--> YES --> WAITING_FOR_APPROVAL
      |
      +--> NO  --> APPROVED
```

The lifecycle transitions themselves are Domain rules and must not be
decided by persistence adapters.

------------------------------------------------------------------------

# 5. Common Service Processing Pattern

Every Transfer service is documented using the same structure:

``` text
1. Input Domain Models
2. Resolve / Enrich authoritative entities
3. Validate existence
4. Validate relationships / ownership
5. Validate authorization or actor context
6. Validate use-case-specific business rules
7. Execute Domain behavior
8. Persist Domain Model
9. Register Operation
10. Register AuditLog
11. Return Domain Model
```

Not every step applies identically to every use case.

For example:

-   a consultation does not mutate the Domain Model and therefore does
    not normally generate an Operation/AuditLog;
-   expiration may be executed by a scheduled/application process and
    may not require an interactive `User`;
-   creation has a concrete enrichment/validation implementation
    described below.

The pattern defines the **shape of the orchestration**, not a claim that
every service currently implements every step.

------------------------------------------------------------------------

# 6. Create Transfer

## 6.1 Description

Creates a transfer request between two bank accounts and establishes its
initial status.

The current implementation is the **reference implementation for
Transfer validation and enrichment orchestration**.

Its behavior is:

``` text
Transfer + User + Customer
          |
          v
Resolve authoritative User
          |
          v
Resolve Customer context
          |
          v
Resolve source BankAccount
          |
          v
Resolve destination BankAccount
          |
          v
Validate accounts and ownership
          |
          v
Assign creation date
          |
          v
Determine approval requirement
          |
          v
Assign initial status
          |
          v
Persist Transfer
          |
          v
Register TRANSFER_CREATION
          |
          v
Return saved Transfer
```

## 6.2 Input

The service receives:

``` text
Transfer transfer
User user
Customer customer
```

Conceptual Input Port:

``` java
interface CreateTransferUseCase {

    Transfer execute(
        Transfer transfer,
        User user,
        Customer customer
    );
}
```

The `Transfer` contains the information required to create the request.

The `User` identifies the requesting actor.

The `Customer` provides the customer context when it is not already
available from the requesting `User`.

## 6.3 Entity Enrichment

### 6.3.1 User enrichment

The service first resolves the authoritative stored `User`:

``` text
User input
   |
   v
UserRepositoryPort.findById(user)
   |
   +--> User not found -> EntityNotFoundException
   |
   v
Stored User
```

The stored Domain Model becomes the authoritative user context for the
remainder of the operation.

### 6.3.2 Customer resolution

The customer context is resolved using:

``` text
if customer != null
    use supplied customer
else if storedUser.customer != null
    use storedUser.customer
else
    reject request
```

If neither a `Customer` nor an associated `User.customer` is available:

``` text
DomainException
"Either customer or requesting user must be provided."
```

### 6.3.3 Account enrichment

The source and destination accounts are resolved through:

``` text
BankAccountRepositoryPort.findByIdentifier(...)
```

Conceptually:

``` text
Transfer
   |
   +--> sourceAccount reference
   |        |
   |        v
   |   BankAccountRepositoryPort
   |        |
   |        v
   |   source : BankAccount
   |
   +--> destinationAccount reference
            |
            v
       BankAccountRepositoryPort
            |
            v
       destination : BankAccount
```

If either account cannot be resolved, the service raises an
`EntityNotFoundException`.

## 6.4 Validations

The current implementation explicitly validates:

### Source account

``` text
source.accountStatus == ACTIVE
```

Otherwise:

``` text
DomainException
"Source account is not active."
```

### Destination account

``` text
destination.accountStatus == ACTIVE
```

Otherwise:

``` text
DomainException
"Destination account is not active."
```

### Different accounts

``` text
source.identifier != destination.identifier
```

Otherwise:

``` text
DomainException
"Source and destination accounts must be different."
```

### Sufficient balance

``` text
source.currentBalance >= transfer.amount
```

Otherwise:

``` text
InsufficientBalanceException
```

### Customer ownership

The resolved source account owner must correspond to the resolved
customer:

``` text
source.owner.identification
        ==
customer.identification
```

Otherwise:

``` text
DomainException
"The provided customer does not own the source bank account."
```

## 6.5 Important Current-Implementation Boundary

The current `CreateTransferService` does **not** explicitly implement
the following validations that appeared in the previous conceptual
specification:

-   explicit `amount > 0` validation;
-   currency compatibility validation;
-   a separate authorization-port call for the creating user.

They must therefore not be documented as currently executed validations
in this service.

They may be introduced later as explicit business rules, but doing so
would constitute a code change rather than a documentation change.

The current service always validates sufficient source balance.

## 6.6 Domain Mutation

The service assigns:

``` text
creationDate = LocalDateTime.now()
```

It then evaluates the configurable approval threshold:

``` text
transfer.amount
      |
      v
BusinessConfigurationPort
      |
      v
getTransferApprovalThreshold()
```

The decision is:

``` text
transfer.amount > threshold
        |
        +--> YES --> WAITING_FOR_APPROVAL
        |
        +--> NO  --> APPROVED
```

The threshold must not be hardcoded.

## 6.7 Persistence

After validation and status assignment:

``` text
TransferRepositoryPort.save(transfer)
```

The returned saved Domain Model is used as the affected product for the
subsequent operation.

## 6.8 Operation and Audit

Creation generates:

``` text
OperationType.TRANSFER_CREATION
```

The operation contains conceptually:

``` text
operationType = TRANSFER_CREATION
executionDate = now
performedBy = transfer.createdBy
affectedProduct = saved Transfer
```

The service delegates operation and audit registration to:

``` text
RegisterOperationAndAuditService
```

The current audit details include:

``` text
amount
status
```

Where:

``` text
amount = transfer.amount
status = saved.transferStatus.code
```

## 6.9 Return

Returns:

``` text
saved Transfer
```

------------------------------------------------------------------------

# 7. Execute Transfer

## 7.1 Description

Executes a transfer that is already approved.

The intended orchestration is:

``` text
User + Transfer
      |
      v
Resolve authoritative context
      |
      v
Validate transfer state
      |
      v
Resolve source/destination accounts
      |
      v
Validate accounts and balance
      |
      v
Debit source
      |
      v
Credit destination
      |
      v
Set EXECUTED
      |
      v
Persist account/transfer changes
      |
      v
Register TRANSFER_EXECUTION
      |
      v
Return Transfer
```

## 7.2 Input

``` text
User
Transfer
```

Conceptual Input Port:

``` java
interface ExecuteTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

## 7.3 Entity Enrichment

When authoritative account state is required, the service resolves:

``` text
source BankAccount
destination BankAccount
```

through:

``` text
BankAccountRepositoryPort
```

The service must validate against the authoritative balances and
statuses rather than relying on stale input state.

If the concrete implementation also requires authoritative user
information, it must resolve the user through:

``` text
UserRepositoryPort
```

following the same enrichment principle established by
`CreateTransferService`.

## 7.4 Validations

The service must validate, according to the current Transfer
specification:

-   transfer exists;
-   transfer status is `APPROVED`;
-   source account is `ACTIVE`;
-   destination account is `ACTIVE`;
-   source account has sufficient balance;
-   `APPROVED -> EXECUTED` is a valid Domain transition;
-   executing actor is authorized according to the applicable
    authorization rules.

## 7.5 Domain Behavior

The transfer execution must produce:

``` text
source balance -= transfer amount
destination balance += transfer amount
transfer status = EXECUTED
```

Balance changes must be performed through Domain behavior and not by
direct database manipulation.

## 7.6 Persistence

The service persists the affected Domain Models through:

``` text
BankAccountRepositoryPort
TransferRepositoryPort
```

The exact atomicity/transaction mechanism belongs to the
application/infrastructure implementation and is outside this Domain
Services document.

## 7.7 Operation and Audit

Generate:

``` text
OperationType.TRANSFER_EXECUTION
```

The audit information should provide traceability for:

``` text
amount
balanceBeforeOrigin
balanceAfterOrigin
balanceBeforeDestination
balanceAfterDestination
```

The operation/audit registration should follow the same orchestration
principle used by `CreateTransferService`.

## 7.8 Return

Returns the executed:

``` text
Transfer
```

------------------------------------------------------------------------

# 8. Submit Transfer for Approval

## 8.1 Description

Places a transfer in:

``` text
WAITING_FOR_APPROVAL
```

when the applicable business rules require approval.

In the current creation implementation, this status is assigned directly
during `Create Transfer` when the amount exceeds the configured
threshold.

Therefore, this service must not be confused with the creation-time
approval decision.

## 8.2 Input

``` text
Transfer
```

If an explicit application use case is retained, its Input Port should
be:

``` java
interface SubmitTransferForApprovalUseCase {

    Transfer execute(Transfer transfer);
}
```

## 8.3 Entity Enrichment

Resolve the authoritative `Transfer` when the supplied model is not
authoritative.

Use:

``` text
TransferRepositoryPort
```

when the use case is invoked independently from creation.

## 8.4 Validations

The service validates:

-   transfer exists;
-   current status allows transition to `WAITING_FOR_APPROVAL`;
-   transfer actually requires approval according to the applicable
    business rule.

If the approval decision is based on amount, the threshold must come
from:

``` text
BusinessConfigurationPort
```

and must not be hardcoded.

## 8.5 Domain Behavior

Transition:

``` text
current status
      |
      v
WAITING_FOR_APPROVAL
```

The Domain Model remains responsible for validating that the state
transition is legal.

## 8.6 Persistence

``` text
TransferRepositoryPort
```

## 8.7 Operation and Audit

If this action is only the internal result of `Create Transfer`, it is
already represented by:

``` text
TRANSFER_CREATION
```

If the application exposes it as an independent business action, a
dedicated audit/operation decision must be made consistently with the
available `OperationType` catalog.

The service must not invent a new `OperationType` that is not supported
by the Domain Model.

## 8.8 Return

``` text
Transfer
```

------------------------------------------------------------------------

# 9. Approve Transfer

## 9.1 Description

Approves a transfer waiting for authorization and moves it to:

``` text
APPROVED
```

## 9.2 Input

``` text
User
Transfer
```

Conceptual Input Port:

``` java
interface ApproveTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

## 9.3 Entity Enrichment

When the supplied entities are not authoritative, resolve:

``` text
User
Transfer
```

through their respective Output Ports.

The authoritative transfer state is required before applying the
approval transition.

## 9.4 Actor Validation

The approving user must be active and have the required authorization.

For the business-transfer scenario defined in the current specification:

``` text
User.role == BUSINESS_SUPERVISOR
AND
User.status == ACTIVE
AND
Transfer.transferStatus == WAITING_FOR_APPROVAL
```

The authorization policy must remain within the appropriate
authorization abstraction when additional external information is
required.

## 9.5 Domain Validations

Validate:

-   transfer exists;
-   transfer status is `WAITING_FOR_APPROVAL`;
-   approving user is not the creator when the applicable
    segregation-of-duties rule applies;
-   `WAITING_FOR_APPROVAL -> APPROVED` is a valid transition.

## 9.6 Domain Behavior

Set:

``` text
transfer.transferStatus = APPROVED
transfer.approvalDate = current date/time
transfer.approvedBy = user
```

The exact state transition must be accepted by the Domain Model.

## 9.7 Persistence

``` text
TransferRepositoryPort
```

## 9.8 Operation and Audit

Generate:

``` text
OperationType.TRANSFER_APPROVAL
```

Audit details should include:

``` text
previousStatus
newStatus
approvedBy
approvalDate
```

## 9.9 Return

``` text
Transfer
```

------------------------------------------------------------------------

# 10. Reject Transfer

## 10.1 Description

Rejects a transfer that is awaiting approval.

## 10.2 Input

``` text
User
Transfer
```

Conceptual Input Port:

``` java
interface RejectTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

## 10.3 Entity Enrichment

Resolve authoritative:

``` text
User
Transfer
```

when the supplied models do not contain authoritative state required by
the operation.

## 10.4 Actor Validation

The rejecting user must have the authorization required for transfer
rejection.

The authorization rules must be resolved consistently with the approval
process.

## 10.5 Domain Validations

The current specification defines:

-   transfer exists;
-   transfer status is `WAITING_FOR_APPROVAL` or another explicitly
    supported rejection state;
-   rejection is a valid transition from the current status.

The service must not introduce additional lifecycle states unless they
are supported by `TransferStatus`.

## 10.6 Domain Behavior

Transition:

``` text
WAITING_FOR_APPROVAL
        |
        v
REJECTED
```

The Domain Model is responsible for validating the transition.

## 10.7 Persistence

``` text
TransferRepositoryPort
```

## 10.8 Operation and Audit

Generate:

``` text
OperationType.TRANSFER_REJECTION
```

Audit details should include:

``` text
previousStatus
newStatus
rejectedBy
rejectionDate
```

## 10.9 Return

``` text
Transfer
```

------------------------------------------------------------------------

# 11. Expire Transfer

## 11.1 Description

Marks a transfer as expired when it remains in:

``` text
WAITING_FOR_APPROVAL
```

beyond the permitted approval period.

## 11.2 Input

``` text
Transfer
```

Conceptual Input Port:

``` java
interface ExpireTransferUseCase {

    Transfer execute(Transfer transfer);
}
```

## 11.3 Entity Enrichment

Resolve the authoritative transfer when required:

``` text
TransferRepositoryPort
```

If expiration requires configurable timing information, obtain it
through:

``` text
BusinessConfigurationPort
```

## 11.4 Validations

Validate:

-   transfer exists;
-   transfer status is `WAITING_FOR_APPROVAL`;
-   the approval window has elapsed;
-   `WAITING_FOR_APPROVAL -> EXPIRED` is a valid Domain transition.

## 11.5 Expiration Rule

The expiration decision must use the configured approval period when
that period is externally configurable.

Conceptually:

``` text
creation/approval reference time
             +
configured approval period
             |
             v
expiration instant
             |
             v
current time >= expiration instant
```

The configuration must not be hardcoded in the service.

## 11.6 Domain Behavior

Transition:

``` text
WAITING_FOR_APPROVAL
        |
        v
EXPIRED
```

Expiration does not move funds.

``` text
source balance: unchanged
destination balance: unchanged
```

## 11.7 Persistence

``` text
TransferRepositoryPort
```

## 11.8 Operation and Audit

Generate:

``` text
OperationType.TRANSFER_EXPIRATION
```

Audit details should include:

``` text
reason
expirationDate
```

## 11.9 Return

``` text
Transfer
```

------------------------------------------------------------------------

# 12. Consult Transfer

## 12.1 Description

Retrieves a `Transfer` Domain Model.

Consultation is read-only and therefore does not mutate the transfer
lifecycle.

## 12.2 Input

``` text
User
Transfer
```

The requesting `User` is required when authorization must be evaluated.

Conceptual Input Port:

``` java
interface ConsultTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

## 12.3 Entity Enrichment

Resolve the authoritative transfer through:

``` text
TransferRepositoryPort
```

If authorization requires additional authoritative context, resolve the
corresponding Domain Models through their Output Ports.

## 12.4 Validations

Validate:

-   transfer exists;
-   requesting user exists/has an applicable system identity when
    required;
-   access to the transfer is permitted;
-   any required relationship between the user and accounts involved in
    the transfer is valid.

## 12.5 Domain Behavior

No Domain mutation is performed.

``` text
Transfer
   |
   v
TransferRepositoryPort
   |
   v
Transfer
```

## 12.6 Persistence

Read only:

``` text
TransferRepositoryPort
```

No update is performed.

## 12.7 Operation and Audit

Consultation does not normally generate a business `Operation` or
`AuditLog`, unless an explicit security/audit policy defines read
auditing as a business requirement.

## 12.8 Return

``` text
Transfer
```

The returned object must be a Domain Model and not a persistence entity.

------------------------------------------------------------------------

# 13. Input Ports

The Transfer subdomain exposes these use cases:

``` text
CreateTransferUseCase
ExecuteTransferUseCase
SubmitTransferForApprovalUseCase
ApproveTransferUseCase
RejectTransferUseCase
ExpireTransferUseCase
ConsultTransferUseCase
```

Recommended contracts:

``` java
public interface CreateTransferUseCase {

    Transfer execute(
        Transfer transfer,
        User user,
        Customer customer
    );
}
```

``` java
public interface ExecuteTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

``` java
public interface SubmitTransferForApprovalUseCase {

    Transfer execute(
        Transfer transfer
    );
}
```

``` java
public interface ApproveTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

``` java
public interface RejectTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

``` java
public interface ExpireTransferUseCase {

    Transfer execute(
        Transfer transfer
    );
}
```

``` java
public interface ConsultTransferUseCase {

    Transfer execute(
        User user,
        Transfer transfer
    );
}
```

The exact method names may follow the project's naming convention, but
the Input Ports must preserve the Domain Model contract.

------------------------------------------------------------------------

# 14. Output Ports

## 14.1 TransferRepositoryPort

Responsible for persistence and retrieval of `Transfer` Domain Models.

Conceptual capabilities:

``` java
public interface TransferRepositoryPort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByIdentifier(Transfer transfer);

    List<Transfer> findBySourceAccount(BankAccount account);

    List<Transfer> findByDestinationAccount(BankAccount account);

    List<Transfer> findPendingApproval();

    List<Transfer> findExpiredCandidates();

    void update(Transfer transfer);
}
```

The exact methods must match the actual project interface. This document
must not require methods that the implementation does not expose.

## 14.2 BankAccountRepositoryPort

Provides authoritative `BankAccount` Domain Models required by Transfer
services.

Used for:

``` text
- resolving source account;
- resolving destination account;
- validating current account status;
- validating current balance;
- persisting account changes during execution.
```

Example capability used by `CreateTransferService`:

``` java
Optional<BankAccount> findByIdentifier(...);
```

## 14.3 UserRepositoryPort

Provides authoritative `User` Domain Models when the actor supplied to a
service must be resolved.

`CreateTransferService` explicitly uses:

``` java
Optional<User> findById(User user);
```

The exact interface signature is governed by the actual project code.

## 14.4 BusinessConfigurationPort

Provides configurable business values required by Transfer services.

The current creation implementation uses:

``` java
BigDecimal getTransferApprovalThreshold();
```

The threshold must remain external/configurable and must not be
hardcoded.

If approval expiration is also configurable, the port may expose the
corresponding capability, provided that it exists in the project
contract.

------------------------------------------------------------------------

# 15. Operation and Audit Integration

Transfer services that mutate the Transfer lifecycle must follow this
general pattern:

``` text
Validate
   |
   v
Enrich
   |
   v
Authorize
   |
   v
Execute Domain Behavior
   |
   v
Persist
   |
   v
Register Operation
   |
   v
Register AuditLog
   |
   v
Return Domain Model
```

The operation must reference the affected `Transfer` as a
`BankingProduct`.

The available Transfer operation types include:

``` text
TRANSFER_CREATION
TRANSFER_APPROVAL
TRANSFER_REJECTION
TRANSFER_EXECUTION
TRANSFER_EXPIRATION
```

The Operation/Audit service is responsible for the cross-cutting
persistence/orchestration of these records.

A Transfer service must not directly depend on MongoDB or another audit
database.

------------------------------------------------------------------------

# 16. Service Validation Matrix

  -------------------------------------------------------------------------------------------------------------------------------------------------------------
  Service    Actor               Input        Enrichment               Main validations        Mutation               Persistence   Operation     Audit
  ---------- ------------------- ------------ ------------------------ ----------------------- ---------------------- ------------- ------------- -------------
  Create     User + Customer     Transfer +   User, Customer, source   User existence, account Set date/status        Transfer      Yes           Yes
  Transfer   context             User +       account, destination     ACTIVE, different                                                          
                                 Customer     account                  accounts, sufficient                                                       
                                                                       balance, source                                                            
                                                                       ownership                                                                  

  Execute    User                User +       Transfer/accounts as     APPROVED, account       Debit, credit,         Accounts +    Yes           Yes
  Transfer                       Transfer     required                 ACTIVE, sufficient      EXECUTED               Transfer                    
                                                                       balance, authorization,                                                    
                                                                       valid transition                                                           

  Submit for Context-dependent   Transfer     Transfer/configuration   Valid transition,       WAITING_FOR_APPROVAL   Transfer      Conditional   Conditional
  Approval                                    as required              approval required                                                          

  Approve    User                User +       User + authoritative     Authorization,          APPROVED, approval     Transfer      Yes           Yes
  Transfer                       Transfer     Transfer                 WAITING_FOR_APPROVAL,   data                                               
                                                                       valid transition                                                           

  Reject     User                User +       User + authoritative     Authorization, valid    REJECTED               Transfer      Yes           Yes
  Transfer                       Transfer     Transfer                 rejection                                                                  
                                                                       state/transition                                                           

  Expire     System/process      Transfer     Transfer + configuration WAITING_FOR_APPROVAL,   EXPIRED                Transfer      Yes           Yes
  Transfer   context                          as required              expiration elapsed,                                                        
                                                                       valid transition                                                           

  Consult    User                User +       Authoritative            Existence and access    None                   Read only     No            No
  Transfer                       Transfer     Transfer/context as                                                                                 
                                              required                                                                                            
  -------------------------------------------------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

# 17. Exception and Error Handling

Transfer services should express business/application failures through
Domain/Application exceptions.

The current creation implementation explicitly uses:

``` text
EntityNotFoundException
DomainException
InsufficientBalanceException
```

Examples:

``` text
User not found
Source account not found
Destination account not found
Either customer or requesting user must be provided
Source account is not active
Destination account is not active
Source and destination accounts must be different
Insufficient balance
Customer does not own source account
```

Exception messages are implementation details and may evolve, but the
business meaning of the failure must remain explicit.

------------------------------------------------------------------------

# 18. Architectural Flow

## 18.1 General inbound flow

``` text
External Request
       |
       v
Input Adapter
       |
       v
Request Mapper
       |
       v
Domain Models
       |
       v
Input Port
       |
       v
Transfer Service
```

REST DTOs therefore stop at the adapter/application boundary.

## 18.2 Create Transfer flow

``` text
HTTP / Application Request
          |
          v
Request Mapper
          |
          v
Transfer + User + Customer
          |
          v
CreateTransferUseCase
          |
          v
CreateTransferService
          |
          +--> UserRepositoryPort
          |       |
          |       v
          |    User
          |
          +--> Customer context resolution
          |
          +--> BankAccountRepositoryPort
          |       |
          |       +--> Source BankAccount
          |       |
          |       +--> Destination BankAccount
          |
          +--> Validate
          |
          +--> BusinessConfigurationPort
          |       |
          |       v
          |   Approval Threshold
          |
          +--> TransferRepositoryPort
          |       |
          |       v
          |   Saved Transfer
          |
          +--> RegisterOperationAndAuditService
          |       |
          |       +--> Operation
          |       |
          |       +--> AuditLog
          |
          v
       Transfer
```

------------------------------------------------------------------------

# 19. Consistency Rules

The Transfer Services document must remain synchronized with:

1.  `Transfer` Domain Model.
2.  `TransferStatus`.
3.  `OperationType`.
4.  `TransferRepositoryPort`.
5.  `BankAccountRepositoryPort`.
6.  `UserRepositoryPort`.
7.  `BusinessConfigurationPort`.
8.  Authorization rules.
9.  Operation and Audit services.
10. The actual service implementations.

## 19.1 Implementation is authoritative for implemented behavior

When documenting a service that already has an implementation, the
implementation is the source of truth for:

-   processing order;
-   entity enrichment;
-   entity resolution;
-   implemented validations;
-   state assignment;
-   repository calls;
-   operation creation;
-   audit details.

The documentation must not silently add behavior that the code does not
execute.

## 19.2 Specification is authoritative for intended behavior not yet implemented

For services without a concrete implementation available for
verification, this document describes the behavior established by the
current domain/service specification.

Such behavior must be verified against the implementation before being
treated as implemented functionality.

## 19.3 No artificial reconciliation

If code and specification differ:

``` text
Code behavior
     |
     v
Document as implemented behavior
```

and, separately:

``` text
Specification requirement
     |
     v
Document as intended/future behavior
```

The two must not be silently merged.

------------------------------------------------------------------------

# 20. Reference Pattern for New Transfer Services

Every new Transfer service should explicitly define:

``` text
Service Name
    |
    +--> Description
    +--> Input Domain Models
    +--> Entity Enrichment
    +--> Existence Validation
    +--> Related Entity Validation
    +--> Relationship / Ownership Validation
    +--> Actor / Authorization Validation
    +--> Domain Validation
    +--> Domain Behavior
    +--> Persistence
    +--> Operation
    +--> Audit
    +--> Exceptions
    +--> Return Value
```

The sections that do not apply to a read-only or system-triggered
operation must be explicitly marked as not applicable rather than
omitted.

This establishes a consistent documentation pattern across the Transfer
subdomain and aligns the services with the Hexagonal Architecture + DDD
principles of domain-first design, dependency inversion, technological
independence, high cohesion, and low coupling.
