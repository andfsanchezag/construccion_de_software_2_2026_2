# Bank Account Services

## Introduction

This document defines the services belonging to the **Bank Account** subdomain of the Banking Information Management System.

The services in this subdomain are responsible for managing the lifecycle and business operations associated with bank accounts.

The main responsibilities include:

- Opening bank accounts.
- Consulting bank accounts.
- Depositing funds.
- Withdrawing funds.
- Blocking accounts.
- Unblocking accounts.
- Closing accounts.
- Managing account balances.
- Validating account ownership.
- Validating account status.
- Registering account-related operations for auditing.

Bank accounts are represented by the `BankAccount` Domain Model and inherit from `BankingProduct`.

```text
BankingProduct
      │
      └── BankAccount
````

Every significant operation performed over a bank account must be represented as a business `Operation` and, when required by the business rules, recorded in the `AuditLog`.

---

# Domain Model Context

A `BankAccount` represents a banking product owned by a `Customer`.

Conceptually:

```text
BankingProduct
      │
      └── BankAccount
             │
             ├── accountType
             ├── owner : Customer
             ├── currentBalance
             ├── currency
             ├── accountStatus
             └── openingDate
```

The account owner must be represented using the `Customer` Domain Model.

The relationship must not be represented as a primitive identifier such as:

```text
String customerId
```

Instead:

```text
Customer owner
```

The persistence adapter is responsible for translating this Domain relationship into the database representation.

---

# Service Design Principles

## Domain Model Parameters

All Bank Account services and Input Ports must receive Domain Models or Value Objects.

They must never receive:

* `String` identifiers.
* Primitive identifiers.
* Individual attributes as substitutes for Domain Models.
* Request DTOs.
* Persistence entities.

### Incorrect

```java
openAccount(
    String customerId,
    AccountType accountType,
    Currency currency
);
```

### Correct

```java
openAccount(BankAccount bankAccount);
```

The same principle applies to every service.

---

# External Information

A service must validate information directly against the Domain Model whenever possible.

For example, the following information can be validated without consulting the database:

```text
BankAccount
├── accountType
├── owner
├── currentBalance
├── currency
├── accountStatus
└── openingDate
```

If information external to the Domain Model is required, the service must use an Output Port.

Example:

```text
BankAccount
     │
     ▼
Bank Account Service
     │
     ▼
CustomerRepository
     │
     ▼
Persistence Adapter
     │
     ▼
Database
```

The service must never access the database directly.

---

# 1. Open Bank Account

## Description

Creates a new `BankAccount` associated with a `Customer`.

The operation establishes the account as a banking product and initializes its business state.

---

## Input

```text
BankAccount
```

The Domain Model must contain:

* Account type.
* Owner.
* Currency.
* Initial balance when applicable.
* Account status.
* Opening date.

The service must not receive these attributes as independent parameters.

---

## Domain Validations

The service validates the account using the information contained in the Domain Model.

Examples include:

* Valid account type.
* Valid currency.
* Valid owner.
* Valid initial balance.
* Valid initial account status.
* Valid opening date.

---

## Customer Validation

The owner is represented by:

```text
BankAccount.owner : Customer
```

If the customer information required by the business rule is already present in the Domain Model, the service validates it directly.

If additional persisted information is required, the service uses:

```text
CustomerRepository
```

through an Output Port.

---

## Account Identifier

The `BankAccount` inherits its product identity from `BankingProduct`.

The service must not generate or manipulate persistence identifiers directly.

The persistence mechanism is responsible for the technical representation of the identifier.

---

## Persistence

After successful validation, the account is persisted through:

```text
BankAccountRepository
```

---

## Operation and Audit

Opening an account represents a business operation.

Conceptually:

```text
BankAccount
     │
     ▼
Open Bank Account
     │
     ├── Persist Account
     │
     └── Create Operation
             │
             ▼
          AuditLog
```

The service must not access MongoDB directly.

Audit persistence must occur through the corresponding Output Port.

---

# 2. Consult Bank Account

## Description

Retrieves information about an existing bank account.

The result must be represented using the `BankAccount` Domain Model.

Persistence entities must never be returned outside the persistence adapter.

---

## Input

```text
BankAccount
```

The service must not receive:

```java
consultAccount(String accountId);
```

as the application-level contract.

Instead:

```java
consultAccount(BankAccount bankAccount);
```

The Domain Model contains the information required to identify or contextualize the requested account.

---

## Processing

```text
BankAccount
     │
     ▼
BankAccountRepository
     │
     ▼
BankAccount
```

---

# 3. Deposit Funds

## Description

Deposits funds into a bank account.

The operation increases the account's `currentBalance`.

The service must validate all business rules before modifying the account.

---

## Input

```text
BankAccount
```

The Domain Model must contain the information necessary to represent the deposit operation.

The amount must be represented using the appropriate Domain Model/Value Object rather than being passed as an isolated primitive parameter.

---

## Validations

The service validates:

* Account status.
* Account eligibility for deposits.
* Deposit amount.
* Currency compatibility when applicable.
* Customer/account relationship when required.

For example, a closed account must not accept deposits.

---

## Balance Modification

The balance modification must be performed through the Domain Model behavior.

Conceptually:

```text
BankAccount
     │
     ▼
Validate Deposit
     │
     ▼
Increase currentBalance
```

The service must not manipulate database fields directly.

---

## Persistence

After the Domain Model reaches a valid state:

```text
BankAccountRepository
```

is used to persist the updated account.

---

## Operation and Audit

A deposit represents a business operation.

Conceptually:

```text
Deposit
   │
   ▼
BankAccount
   │
   ├── Update Balance
   │
   └── Operation
          │
          ▼
       AuditLog
```

---

# 4. Withdraw Funds

## Description

Withdraws funds from a bank account.

The operation decreases the account's `currentBalance`.

---

## Input

```text
BankAccount
```

The withdrawal information must be represented through the Domain Model.

The service must not receive:

```java
withdraw(
    String accountId,
    BigDecimal amount
);
```

Instead, the required information must be represented through the appropriate Domain Model.

---

## Validations

The service validates:

* Account status.
* Withdrawal amount.
* Available balance.
* Currency compatibility when applicable.
* Account eligibility for withdrawals.

---

## Insufficient Balance

The account must not allow a withdrawal that exceeds its available balance.

Conceptually:

```text
currentBalance >= withdrawalAmount
```

If this rule is violated:

```text
InsufficientBalanceException
```

must be raised.

---

## Balance Modification

When all validations succeed:

```text
BankAccount
     │
     ▼
Validate Withdrawal
     │
     ▼
Decrease currentBalance
```

The Domain Model remains responsible for maintaining its own valid state.

---

## Persistence

The updated account is persisted through:

```text
BankAccountRepository
```

---

## Operation and Audit

A withdrawal represents a business operation and must be traceable.

```text
Withdrawal
    │
    ▼
BankAccount
    │
    ├── Update Balance
    │
    └── Operation
           │
           ▼
        AuditLog
```

---

# 5. Block Bank Account

## Description

Changes the operational status of a bank account to `BLOCKED`.

A blocked account cannot perform operations that are prohibited by its status.

---

## Input

```text
BankAccount
```

The service must not receive:

```java
blockAccount(String accountId);
```

---

## Domain Validation

The service validates whether the current account state allows the transition to:

```text
BLOCKED
```

The valid status transition is determined by Domain rules.

---

## Status Change

Conceptually:

```text
BankAccount
     │
     ▼
Validate Status Transition
     │
     ▼
AccountStatus = BLOCKED
```

---

## Persistence

The updated account is persisted through:

```text
BankAccountRepository
```

---

## Operation and Audit

Blocking an account is a significant business operation.

It must generate an `Operation` and the corresponding audit information according to the audit rules.

```text
Block Account
      │
      ├── Update Account
      │
      └── Operation
             │
             ▼
          AuditLog
```

---

# 6. Unblock Bank Account

## Description

Changes a blocked bank account back to an operational state according to the permitted Domain transition.

---

## Input

```text
BankAccount
```

---

## Validations

The service validates:

* Current account status.
* Validity of the requested transition.
* Authorization of the actor when required.
* Any additional business conditions.

External information must be obtained through Output Ports.

---

## Status Change

Conceptually:

```text
BankAccount
     │
     ▼
AccountStatus = BLOCKED
     │
     ▼
Validate Transition
     │
     ▼
AccountStatus = ACTIVE
```

---

## Persistence and Audit

After the transition succeeds:

```text
BankAccountRepository
```

persists the updated account.

The operation must also be registered according to the Operation and Audit rules.

---

# 7. Close Bank Account

## Description

Permanently closes a bank account.

The account status changes to:

```text
CLOSED
```

---

## Input

```text
BankAccount
```

---

## Validations

The service validates the conditions required to close the account.

These validations must use information available in the Domain Model whenever possible.

Possible business conditions include:

* Account status allows closure.
* Account has no prohibited outstanding balance.
* Account is not involved in an operation that prevents closure.
* Required authorization is available.

If a required condition depends on external information, the service must use the appropriate Output Port.

---

## Status Change

```text
BankAccount
     │
     ▼
Validate Closure
     │
     ▼
AccountStatus = CLOSED
```

A closed account must not return to an active state unless the Domain explicitly defines such a transition.

---

## Persistence and Audit

The account is persisted through:

```text
BankAccountRepository
```

The closure is recorded as a business operation and audited.

---

# 8. Validate Account Ownership

## Description

Validates that a `Customer` owns or is authorized to operate on a specific `BankAccount`.

The relationship is represented by the Domain Models:

```text
BankAccount.owner
```

and:

```text
Customer
```

The service must not compare raw identifiers directly as a substitute for the Domain relationship.

---

## Input

The service receives the appropriate Domain Models required by the ownership rule.

For example:

```text
BankAccount
Customer
```

If the architectural rule requires a single Domain Model parameter, the operation should be represented through an appropriate Domain Model containing the required relationship rather than passing primitive identifiers.

---

## External Validation

If the ownership cannot be determined from the provided Domain Model, the service uses:

```text
BankAccountRepository
```

or:

```text
CustomerRepository
```

depending on the required business information.

---

# 9. Consult Account Balance

## Description

Provides the current balance of a `BankAccount`.

The balance belongs to the Domain Model:

```text
BankAccount.currentBalance
```

Therefore, if the complete and current `BankAccount` is already available, no external query is required.

If the current persisted state is required, the service retrieves the account through:

```text
BankAccountRepository
```

---

## Domain Rule

The service must not calculate or reconstruct the balance from database records unless the Domain explicitly requires such behavior.

The authoritative balance is the one represented by the `BankAccount` Domain Model.

---

# Output Ports

The Bank Account subdomain requires Output Ports for external dependencies.

Conceptually:

```text
BankAccountRepository
CustomerRepository
OperationRepository
AuditRepository
```

Additional ports may be introduced when future business rules require external information.

---

# BankAccountRepository

## Description

Defines persistence operations required for `BankAccount`.

Conceptually:

```java
interface BankAccountRepository {

    BankAccount save(BankAccount bankAccount);

    BankAccount find(BankAccount bankAccount);

    boolean exists(BankAccount bankAccount);
}
```

The exact methods should be refined according to the persistence requirements.

The interface belongs to:

```text
domain/ports/out/
```

The MySQL adapter implements this port.

---

# CustomerRepository

## Description

Provides customer information when a bank account operation requires information that cannot be determined from the `Customer` Domain Model already associated with the account.

Conceptually:

```java
interface CustomerRepository {

    Customer find(Customer customer);

    boolean exists(Customer customer);
}
```

The exact interface should be defined in the dedicated Customer Output Port documentation.

---

# OperationRepository

## Description

Provides persistence of business operations generated by bank account activities.

The service must not persist operations directly.

Conceptually:

```text
Bank Account Service
        │
        ▼
Operation
        │
        ▼
OperationRepository
        │
        ▼
Persistence Adapter
```

---

# AuditRepository

## Description

Provides persistence of audit records.

Audit records are stored in MongoDB according to the architecture.

The service communicates through the Output Port:

```text
AuditRepository
```

and never accesses MongoDB directly.

---

# Input Ports

The Bank Account subdomain exposes the following conceptual use cases:

```text
OpenBankAccountUseCase
ConsultBankAccountUseCase
DepositFundsUseCase
WithdrawFundsUseCase
BlockBankAccountUseCase
UnblockBankAccountUseCase
CloseBankAccountUseCase
ValidateAccountOwnershipUseCase
ConsultAccountBalanceUseCase
```

Each Input Port must receive Domain Models rather than primitive values.

---

# Example Input Port

```java
interface OpenBankAccountUseCase {

    BankAccount open(BankAccount bankAccount);
}
```

---

# Deposit Flow

```text
BankAccount
     │
     ▼
DepositFundsUseCase
     │
     ▼
Bank Account Service
     │
     ├── Validate Domain Rules
     │
     ├── Update BankAccount
     │
     ├── BankAccountRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```

---

# Withdrawal Flow

```text
BankAccount
     │
     ▼
WithdrawFundsUseCase
     │
     ▼
Bank Account Service
     │
     ├── Validate AccountStatus
     │
     ├── Validate Balance
     │
     ├── Update BankAccount
     │
     ├── BankAccountRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```

---

# Account Blocking Flow

```text
BankAccount
     │
     ▼
BlockBankAccountUseCase
     │
     ▼
Bank Account Service
     │
     ├── Validate Status Transition
     │
     ├── Change AccountStatus
     │
     ├── BankAccountRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```

---

# Input Adapter Flow

External requests must be transformed into Domain Models before entering the Domain.

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
BankAccount Domain Model
     │
     ▼
Input Port
     │
     ▼
Bank Account Service
```

Controllers must not implement account business rules.

---

# Validation Strategy

## Domain Validation

The following information belongs directly to `BankAccount`:

```text
accountType
owner
currentBalance
currency
accountStatus
openingDate
```

Business rules involving these attributes should be validated by the Domain Model or Domain Service.

---

## External Validation

When a rule requires information not contained in the Domain Model, the service must call an Output Port.

Examples:

```text
Customer existence
Existing account
Persisted account state
Operation persistence
Audit persistence
```

The flow must always be:

```text
Domain Service
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

# Account Status

The `BankAccount` uses the `AccountStatus` Value Object.

The supported conceptual states are:

```text
ACTIVE
BLOCKED
CLOSED
```

The Domain is responsible for validating status transitions.

The database must not determine whether a transition is valid.

---

# Account Operations and Audit

Bank accounts are `BankingProduct` entities.

Every significant business movement involving a bank account must generate an `Operation`.

Examples include:

```text
ACCOUNT_OPENING
DEPOSIT
WITHDRAWAL
```

Account state changes such as blocking or closing must also generate an operation when required by the business rules.

Conceptually:

```text
BankAccount
     │
     ▼
Business Action
     │
     ├──────────────► BankAccount updated
     │
     ▼
Operation
     │
     ▼
AuditLog
```

The audit mechanism must remain independent from the Bank Account service implementation.

---

# Relationship With BankingProduct

`BankAccount` inherits from `BankingProduct`.

Therefore, common product-level information must not be duplicated inside `BankAccount`.

Conceptually:

```text
BankingProduct
      │
      ├── identifier
      │
      └── common product information
              │
              ▼
        BankAccount
```

The exact common attributes belong to the `BankingProduct` Domain Model.

---

# Exceptions

Conceptual exceptions for this subdomain include:

```text
BankAccountNotFoundException
InvalidBankAccountException
InvalidAccountStatusException
InvalidAccountStatusTransitionException
InvalidAccountOwnershipException
InsufficientBalanceException
InvalidDepositException
InvalidWithdrawalException
AccountAlreadyClosedException
AccountAlreadyBlockedException
CustomerNotEligibleException
```

The complete exception catalog should be defined separately in the Domain Exceptions documentation.

---

# Architectural Constraints

The following constraints are mandatory for all Bank Account Services:

1. `BankAccount` is a Domain Model.
2. `BankAccount` inherits from `BankingProduct`.
3. The account owner is represented by `Customer`.
4. Customer relationships must not be represented by primitive identifiers in the Domain Model.
5. Services must receive Domain Models or Value Objects.
6. Services must never receive primitive identifiers as application-level parameters.
7. Services must never receive isolated attributes when those attributes belong to a Domain Model.
8. Services must never receive REST DTOs.
9. Services must never receive persistence entities.
10. Controllers must not contain Bank Account business rules.
11. Business validations must belong to the Domain.
12. Information already available in the Domain Model must be validated without unnecessary external calls.
13. Information external to the Domain must be retrieved through Output Ports.
14. Services must always communicate with external resources through Output Ports.
15. Services must never access MySQL directly.
16. Services must never access MongoDB directly.
17. Services must never access SQL or JPA directly.
18. `BankAccountRepository` is responsible for Bank Account persistence.
19. `CustomerRepository` is responsible for external Customer information when required.
20. `OperationRepository` is responsible for operation persistence.
21. `AuditRepository` is responsible for audit persistence.
22. Account balance changes must be performed through valid Domain behavior.
23. Withdrawals must not exceed the available balance.
24. Blocked accounts must respect the restrictions defined by `AccountStatus`.
25. Closed accounts must respect the restrictions defined by `AccountStatus`.
26. Account status transitions must be validated by Domain rules.
27. Significant account operations must generate an `Operation`.
28. Relevant account operations must be recorded in the `AuditLog`.
29. Persistence entities must never leave the persistence adapter.
30. The Domain must remain independent of infrastructure technologies.
31. The entire Bank Account subdomain must be testable without requiring MySQL, MongoDB, REST, or infrastructure components.

```
```
