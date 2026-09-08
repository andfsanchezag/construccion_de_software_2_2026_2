# Bank Account Services

## 1. Introduction

This document defines the services belonging to the **Bank Account Management** subdomain of the Banking Information Management System (BIMS).

The Bank Account Management subdomain is responsible for managing the lifecycle and business operations associated with bank accounts.

The main business capabilities are:

* Open Bank Account.
* Consult Bank Account.
* Consult Account Balance.
* Deposit Funds.
* Withdraw Funds.
* Block Bank Account.
* Unblock Bank Account.
* Close Bank Account.

Bank accounts are represented by the `BankAccount` Domain Model and inherit from `BankingProduct`.

```text
BankingProduct
      |
      +-- BankAccount
```

A Bank Account is owned by a `Customer`.

```text
Customer
    |
    | owns
    v
BankAccount
```

The ownership relationship must be explicitly validated whenever an operation requires access to a customer's Bank Account.

Every significant state-changing operation performed on a Bank Account must generate a business `Operation` and the corresponding `AuditLog` according to the audit rules of the system.

---

# 2. Domain Model Context

## 2.1 BankAccount

`BankAccount` is a Domain Model representing a banking product.

Conceptually:

```text
BankingProduct
      |
      +-- BankAccount
             |
             +-- accountType
             +-- owner : Customer
             +-- currentBalance
             +-- currency
             +-- accountStatus
             +-- openingDate
```

Common attributes that belong to `BankingProduct` must not be duplicated in `BankAccount`.

The exact inheritance structure must be defined by the Domain Model.

---

## 2.2 Bank Account Owner

The owner of a Bank Account is represented by a `Customer` Domain Model.

Correct:

```java
BankAccount.owner : Customer
```

Incorrect:

```java
BankAccount.ownerId : String
```

The Domain Model must represent the business relationship rather than reducing it to a primitive identifier.

Persistence adapters are responsible for translating this relationship into the persistence representation.

---

# 3. User, Customer, and BankAccount Relationship

Bank Account operations involve three different concepts:

```text
User
 |
 | performs operation
 v
Customer
 |
 | owns
 v
BankAccount
```

These concepts must not be treated as interchangeable.

### User

The `User` represents the actor requesting or performing the operation.

### Customer

The `Customer` represents the banking customer associated with the operation.

### BankAccount

The `BankAccount` represents the banking product affected by the operation.

Therefore:

```text
User != Customer
Customer != BankAccount
User != BankAccount
```

A Customer User may be associated with a Customer, while an Employee User may operate on behalf of the bank according to their role and permissions.

---

# 4. Service Design Principle

Each Bank Account service represents one cohesive business operation.

A service is responsible for determining and validating **all business conditions necessary to execute that operation correctly**.

The architecture must not artificially fragment one operation into multiple small services.

For example, it is acceptable for:

```text
WithdrawFundsService
```

to perform:

```text
User validation
Customer validation
Authorization validation
Ownership validation
BankAccount validation
Account status validation
Amount validation
Balance validation
Withdrawal execution
Persistence
Operation registration
Audit registration
```

through private methods or cohesive collaborators.

It is not necessary to create:

```text
ValidateUserService
ValidateCustomerService
ValidateOwnershipService
ValidateAccountStatusService
ValidateBalanceService
ValidateWithdrawalAmountService
```

merely to separate validations.

The important requirement is:

> The application service must prevent the business operation from executing unless every required business condition is satisfied.

---

# 5. Standard Application Service Pattern

State-changing Bank Account services should generally follow this pattern:

```text
Input Domain Models / Value Objects
                |
                v
Retrieve authoritative state
                |
                v
Validate requesting User
                |
                v
Validate Customer
                |
                v
Validate User-Customer relationship
                |
                v
Validate Customer-Product relationship
                |
                v
Validate BankAccount
                |
                v
Validate Account Status
                |
                v
Validate operation-specific business rules
                |
                v
Execute Domain behavior
                |
                v
Persist through Output Port
                |
                v
Register Operation
                |
                v
Register Audit
                |
                v
Return result
```

Not every service requires every validation shown above.

Each service must apply the validations relevant to its operation.

---

# 6. Input Contract

Bank Account application services must operate using Domain Models and Value Objects.

They must not expose REST DTOs or persistence entities as application contracts.

Primitive identifiers must not be used as the primary application-level representation when the domain already provides a corresponding Domain Model or Value Object.

Incorrect:

```java
withdraw(
    String accountId,
    String customerId,
    String userId,
    BigDecimal amount
);
```

Preferred:

```java
withdraw(
    User requestingUser,
    Customer customer,
    BankAccount account,
    Money amount
);
```

The exact signature may vary according to the project's domain model, but the architectural principle is mandatory.

---

# 7. Value Objects

Amounts of money should preferably be represented through a `Money` Value Object.

Conceptually:

```text
Money
 |
 +-- amount
 +-- currency
```

Instead of:

```java
BigDecimal amount
```

the application may use:

```java
Money amount
```

This allows monetary invariants to be encapsulated in the Domain.

Examples of Money rules include:

```text
amount != null
amount > 0
currency != null
```

Currency compatibility between the transaction and the Bank Account must be validated whenever the domain supports multiple currencies.

---

# 8. Authoritative State

Domain Models received by a service represent the operation context, but they must not automatically be considered the authoritative persisted state.

For state-changing operations, the service must retrieve the current Bank Account state through the corresponding Output Port whenever current state is required.

For example:

```text
Input BankAccount
       |
       v
BankAccountRepositoryPort
       |
       v
Authoritative BankAccount
       |
       v
Business Validation
       |
       v
Domain Operation
```

This is particularly important for:

* Current balance.
* Account status.
* Account ownership.
* Product existence.
* Other persisted business state.

The service must not trust a caller-provided balance or account status when the current persisted state is required.

---

# 9. External Information

Information already contained in a Domain Model must be validated from that Domain Model whenever possible.

For example:

```text
BankAccount
 |
 +-- accountType
 +-- owner
 +-- currentBalance
 +-- currency
 +-- accountStatus
 +-- openingDate
```

No external call is required merely to validate information already available and authoritative in the Domain Model.

However, external information required for a business decision must be obtained through an Output Port.

Example:

```text
Bank Account Service
        |
        v
CustomerRepositoryPort
        |
        v
Customer Adapter
        |
        v
External Persistence
```

Application services must never access the database directly.

---

# 10. User Validation

When an operation is performed by a User, the service must validate the User according to the business requirements of that operation.

Relevant validations may include:

* User exists.
* User is active.
* User is authorized.
* User has the required role or permission.
* Customer User is associated with the relevant Customer.
* Employee User is authorized to perform the operation.

The service must not assume:

```text
User exists
    =
User is authorized
```

Authentication and authorization are different concepts.

The User represents the actor, while the Customer represents the banking customer.

---

# 11. Customer Validation

When a Bank Account operation involves a Customer, the service must validate the Customer according to the business requirements.

Relevant validations may include:

* Customer exists.
* Customer is active or otherwise eligible.
* Customer is allowed to perform the operation.
* Customer is the owner of the Bank Account when ownership is required.

The service must not assume:

```text
Customer exists
    =
Customer is eligible
```

When authoritative Customer information is required, it must be obtained through:

```text
CustomerRepositoryPort
```

---

# 12. Customer-BankAccount Ownership

Ownership validation is mandatory whenever the operation is restricted to the account owner.

The service must explicitly validate:

```text
Customer
    |
    | owns
    v
BankAccount
```

The service must not assume ownership merely because:

* A Customer was supplied as an input.
* A Bank Account was supplied as an input.
* Both objects were provided in the same request.
* Their identifiers were supplied by the caller.

The reference implementation represented by `WithdrawFundsService` establishes this principle.

The service retrieves the authoritative Bank Account and Customer and validates that the Customer corresponds to the Bank Account owner.

The important business rule is:

> A customer must not be allowed to operate on a Bank Account that belongs to another customer.

---

# 13. Product Access

Existence, ownership, and authorization are different validations.

The following is insufficient:

```text
BankAccount exists
+
Customer exists
=
Customer can operate on BankAccount
```

The service must establish the complete relationship:

```text
Requesting User
       |
       v
Customer
       |
       | owns
       v
BankAccount
```

For employee operations, authorization may instead follow:

```text
Requesting User
       |
       v
Employee Role / Permission
       |
       v
Authorized Customer
       |
       v
BankAccount
```

The applicable rule depends on the operation and the user's role.

---

# 14. Account Status

The conceptual Bank Account statuses are:

```text
ACTIVE
BLOCKED
CLOSED
```

The Domain is responsible for defining and protecting valid status transitions.

The application service must validate that the requested operation is allowed for the current status.

Example:

```text
ACTIVE
  |
  +-- deposit allowed
  +-- withdrawal allowed
  +-- block allowed

BLOCKED
  |
  +-- deposit prohibited
  +-- withdrawal prohibited
  +-- unblock allowed

CLOSED
  |
  +-- deposit prohibited
  +-- withdrawal prohibited
  +-- unblock prohibited
```

The exact business matrix must follow the domain requirements.

---

# 15. Domain Behavior

Bank Account state must be changed through valid Domain behavior.

Preferred:

```java
bankAccount.deposit(money);
bankAccount.withdraw(money);
bankAccount.block();
bankAccount.unblock();
bankAccount.close();
```

Avoid treating unrestricted setters as business behavior:

```java
bankAccount.setCurrentBalance(...);
bankAccount.setAccountStatus(...);
```

The Domain Model should protect its own invariants.

The application service coordinates the operation:

```text
Retrieve
   ->
Validate
   ->
Execute Domain Behavior
   ->
Persist
```

---

# 16. Operation and Audit

Bank Account operations that modify business state must generate an `Operation`.

Examples include:

```text
ACCOUNT_OPENING
DEPOSIT
WITHDRAWAL
ACCOUNT_BLOCK
ACCOUNT_UNBLOCK
ACCOUNT_CLOSURE
```

The exact operation types must match the project's domain model.

An `Operation` should identify, as applicable:

```text
operationType
executionDate
performedBy
affectedProduct
operation-specific details
```

Relevant operations must also generate an `AuditLog`.

The audit record should provide sufficient information to determine:

```text
who performed the operation
what operation was performed
which product was affected
when it occurred
relevant operation details
```

---

# 17. Transactional Consistency

State-changing operations must maintain consistency between:

```text
BankAccount
Operation
AuditLog
```

The following sequence is potentially unsafe:

```text
Update BankAccount
        |
        v
Register Operation
        |
        X
Register Audit fails
```

This could result in:

```text
BankAccount updated
Operation persisted
Audit missing
```

The implementation must use the appropriate transaction or consistency mechanism so that required state changes and traceability records are handled consistently.

This requirement does not imply creating additional business services.

---

# 18. Input Ports

The Bank Account subdomain exposes the following Input Ports:

```text
OpenBankAccountUseCase
ConsultBankAccountUseCase
ConsultAccountBalanceUseCase
DepositFundsUseCase
WithdrawFundsUseCase
BlockBankAccountUseCase
UnblockBankAccountUseCase
CloseBankAccountUseCase
```

Account ownership validation is a business responsibility of the applicable service.

It does not need to be exposed as an independent business use case merely because ownership is a validation.

Therefore, the following should not automatically be treated as a separate application service:

```text
ValidateAccountOwnershipUseCase
```

The ownership rule is normally executed inside the service requiring it.

---

# 19. Output Ports

The Bank Account subdomain may use the following Output Ports:

```text
BankAccountRepositoryPort
CustomerRepositoryPort
OperationRepositoryPort
AuditRepositoryPort
```

These interfaces belong to the application/domain boundary.

Their implementations belong to adapters.

---

# 20. BankAccountRepositoryPort

`BankAccountRepositoryPort` is responsible for Bank Account persistence and retrieval.

Conceptually:

```java
public interface BankAccountRepositoryPort {

    Optional<BankAccount> findByIdentifier(BankAccount bankAccount);

    BankAccount save(BankAccount bankAccount);

    BankAccount update(BankAccount bankAccount);
}
```

The exact method names and signatures may vary.

The port must operate using Domain Models.

The persistence implementation must remain hidden behind the port.

---

# 21. CustomerRepositoryPort

`CustomerRepositoryPort` provides authoritative Customer information when required by Bank Account operations.

Conceptually:

```java
public interface CustomerRepositoryPort {

    Optional<Customer> findByIdentification(Customer customer);
}
```

The exact contract must follow the Customer subdomain specification.

The Bank Account service may use this port to validate:

* Customer existence.
* Current Customer state.
* Customer eligibility.
* Customer relationship with the Bank Account.

---

# 22. OperationRepositoryPort

`OperationRepositoryPort` is responsible for Operation persistence.

Conceptually:

```text
Bank Account Service
        |
        v
Operation
        |
        v
OperationRepositoryPort
        |
        v
Persistence Adapter
```

The service must never directly access the database.

---

# 23. AuditRepositoryPort

`AuditRepositoryPort` is responsible for AuditLog persistence.

Conceptually:

```text
Bank Account Service
        |
        v
AuditLog
        |
        v
AuditRepositoryPort
        |
        v
Persistence Adapter
        |
        v
MongoDB
```

The Bank Account service must not directly access MongoDB.

---

# 24. Open Bank Account

## 24.1 Purpose

Creates a new Bank Account associated with an eligible Customer.

The operation establishes the Bank Account as a banking product and initializes its valid business state.

---

## 24.2 Input

The operation must receive the appropriate Domain Model representation.

Conceptually:

```text
BankAccount
```

The Bank Account should contain or reference:

* Account type.
* Owner.
* Currency.
* Initial balance when applicable.
* Account status.
* Opening date.

The service must not require these values as unrelated primitive parameters.

---

## 24.3 Validations

The service must validate all rules required to create the account, including when applicable:

* Requesting User.
* User status.
* User authorization.
* Customer existence.
* Customer status.
* Customer eligibility.
* Valid account type.
* Valid currency.
* Valid initial balance.
* Valid initial status.
* Valid opening date.
* Any account-opening restrictions.

---

## 24.4 Customer Validation

The Bank Account owner is:

```text
BankAccount.owner : Customer
```

If the Domain Model already contains all required information, it should be validated directly.

If authoritative external Customer information is required:

```text
CustomerRepositoryPort
```

must be used.

---

## 24.5 Account Creation

The account must be initialized through valid Domain behavior.

The service must not manually manipulate persistence state.

Conceptually:

```text
BankAccount
      |
      v
Validate
      |
      v
Open/Create Domain State
      |
      v
BankAccountRepositoryPort
```

---

## 24.6 Operation and Audit

Opening an account is a significant business operation.

The service must:

1. Persist the new Bank Account.
2. Register the corresponding `Operation`.
3. Register the required `AuditLog`.

These operations must maintain transactional consistency.

---

# 25. Consult Bank Account

## 25.1 Purpose

Retrieves an existing Bank Account to which the requesting actor is authorized to have access.

---

## 25.2 Input

The application-level contract must use the appropriate Domain Model.

Conceptually:

```text
BankAccount
```

The service must not expose:

```java
consultAccount(String accountId);
```

as the business use case contract.

---

## 25.3 Processing

The service should:

```text
1. Validate requesting User.
2. Retrieve authoritative BankAccount.
3. Validate BankAccount existence.
4. Resolve Customer when required.
5. Validate Customer.
6. Validate User authorization.
7. Validate Customer-BankAccount ownership or access relationship.
8. Return BankAccount.
```

---

## 25.4 Persistence

The account must be retrieved through:

```text
BankAccountRepositoryPort
```

Persistence entities must never be returned outside the persistence adapter.

---

# 26. Consult Account Balance

## 26.1 Purpose

Returns the current balance of an authorized Bank Account.

---

## 26.2 Processing

The service must:

```text
1. Validate requesting User.
2. Validate User status.
3. Validate authorization.
4. Retrieve authoritative BankAccount when required.
5. Validate BankAccount existence.
6. Resolve Customer when required.
7. Validate ownership/access.
8. Return currentBalance.
```

---

## 26.3 Balance Authority

The authoritative balance is:

```text
BankAccount.currentBalance
```

The service must not trust a balance supplied by the caller.

The service must not reconstruct the balance from arbitrary persistence data unless explicitly required by the domain.

---

# 27. Deposit Funds

## 27.1 Purpose

Deposits funds into an authorized Bank Account.

The operation increases:

```text
BankAccount.currentBalance
```

---

## 27.2 Input

The operation must use the appropriate Domain Models and Value Objects.

Conceptually:

```text
User
Customer
BankAccount
Money
```

The exact method signature depends on the domain model.

---

## 27.3 Required Validations

The service must validate:

### User

* User exists.
* User is active.
* User is authorized.

### Customer

* Customer exists.
* Customer is active/eligible when required.
* Customer is authorized to operate on the account when applicable.

### Ownership / Access

* Customer owns the Bank Account when customer ownership is required.
* Employee User has the required authorization when operating on behalf of the bank.

### Bank Account

* Account exists.
* Account is in a status that allows deposits.

### Deposit

* Amount exists.
* Amount is greater than zero.
* Currency is compatible when applicable.
* Other deposit-specific business rules are satisfied.

---

## 27.4 Account Status

Under the default rules:

```text
ACTIVE  -> Deposit allowed
BLOCKED -> Deposit rejected
CLOSED  -> Deposit rejected
```

---

## 27.5 Domain Behavior

The balance must be changed through Domain behavior:

```java
bankAccount.deposit(money);
```

The service must not directly calculate and assign the balance.

---

## 27.6 Persistence

After successful Domain execution:

```text
BankAccountRepositoryPort.update(bankAccount)
```

must persist the new state.

---

## 27.7 Operation and Audit

A successful deposit must generate:

```text
Operation
    operationType = DEPOSIT
    performedBy = requestingUser
    affectedProduct = bankAccount
```

Relevant details should include:

```text
amount
balanceBefore
balanceAfter
```

The corresponding audit record must also be generated.

---

# 28. Withdraw Funds

## 28.1 Purpose

Withdraws funds from an authorized Bank Account.

The operation decreases:

```text
BankAccount.currentBalance
```

`WithdrawFundsService` is the reference implementation pattern for state-changing Bank Account services.

---

## 28.2 Input

The operation must use Domain Models and Value Objects.

Conceptually:

```text
User
Customer
BankAccount
Money
```

The service must not expose a primitive application contract such as:

```java
withdraw(
    String accountId,
    BigDecimal amount
);
```

---

## 28.3 Reference Execution Pattern

The reference implementation follows this conceptual flow:

```text
Requesting User
      |
      v
Customer
      |
      v
BankAccount
      |
      v
Retrieve authoritative BankAccount
      |
      v
Validate BankAccount existence
      |
      v
Resolve authoritative Customer
      |
      v
Validate Customer existence
      |
      v
Validate Customer owns BankAccount
      |
      v
Validate User
      |
      v
Validate authorization
      |
      v
Validate AccountStatus
      |
      v
Validate withdrawal amount
      |
      v
Validate sufficient balance
      |
      v
Execute withdrawal
      |
      v
Persist BankAccount
      |
      v
Register Operation
      |
      v
Register Audit
```

---

## 28.4 Required Validations

The service must validate:

### Requesting User

* User is present.
* User exists/represents a valid actor.
* User is active.
* User has the required authorization.

### Customer

* Customer exists.
* Customer is valid.
* Customer is eligible when required.

### Ownership

The Customer involved in the operation must correspond to the Bank Account owner when the operation is customer-restricted.

### Bank Account

* Account exists.
* Current state is authoritative.
* Account status allows withdrawals.

### Withdrawal

* Amount is present.
* Amount is greater than zero.
* Currency is compatible when applicable.
* Amount does not exceed available balance.

---

## 28.5 Insufficient Balance

A withdrawal must satisfy:

```text
withdrawalAmount <= currentBalance
```

Otherwise:

```text
InsufficientBalanceException
```

must be raised.

---

## 28.6 Account Status

Under the default rules:

```text
ACTIVE  -> Withdrawal allowed
BLOCKED -> Withdrawal rejected
CLOSED  -> Withdrawal rejected
```

---

## 28.7 Domain Behavior

The preferred implementation is:

```java
bankAccount.withdraw(money);
```

The Domain Model must guarantee that the Bank Account remains valid after the operation.

---

## 28.8 Operation and Audit

A successful withdrawal must generate an Operation.

Conceptually:

```text
Operation
 |
 +-- operationType = WITHDRAWAL
 +-- performedBy = requestingUser
 +-- affectedProduct = bankAccount
 +-- executionDate
```

Relevant details should include:

```text
amount
balanceBefore
balanceAfter
```

A corresponding AuditLog must be generated according to the audit rules.

---

# 29. Block Bank Account

## 29.1 Purpose

Changes an eligible Bank Account from its current state to:

```text
BLOCKED
```

---

## 29.2 Required Validations

The service must validate:

* Requesting User.
* User status.
* User authorization.
* Bank Account existence.
* Customer relationship when applicable.
* Customer ownership when applicable.
* Current account status.
* Valid status transition.
* Additional blocking business rules.

---

## 29.3 Default Transition

```text
ACTIVE -> BLOCKED
```

The Domain must reject invalid transitions.

Examples:

```text
BLOCKED -> BLOCKED
CLOSED -> BLOCKED
```

unless explicitly permitted by the business rules.

---

## 29.4 Domain Behavior

Preferred:

```java
bankAccount.block();
```

rather than:

```java
bankAccount.setAccountStatus(BLOCKED);
```

---

## 29.5 Operation and Audit

A successful block operation must generate:

```text
Operation
    operationType = ACCOUNT_BLOCK
    performedBy = requestingUser
    affectedProduct = bankAccount
```

and the corresponding audit information.

---

# 30. Unblock Bank Account

## 30.1 Purpose

Changes an eligible blocked Bank Account back to:

```text
ACTIVE
```

---

## 30.2 Required Validations

The service must validate:

* Requesting User.
* User status.
* User authorization.
* Bank Account existence.
* Customer relationship when applicable.
* Customer ownership when applicable.
* Current account status.
* Valid status transition.
* Additional unblocking business rules.

---

## 30.3 Default Transition

```text
BLOCKED -> ACTIVE
```

Invalid examples include:

```text
ACTIVE -> ACTIVE
CLOSED -> ACTIVE
```

unless explicitly permitted by the Domain.

---

## 30.4 Domain Behavior

Preferred:

```java
bankAccount.unblock();
```

---

## 30.5 Operation and Audit

A successful unblock operation must generate an Operation and the corresponding AuditLog according to the audit rules.

---

# 31. Close Bank Account

## 31.1 Purpose

Closes a Bank Account by transitioning it to:

```text
CLOSED
```

---

## 31.2 Required Validations

The service must validate:

### User

* User exists.
* User is active.
* User is authorized.

### Customer

* Customer exists.
* Customer is valid.
* Customer owns the account when ownership is required.

### Bank Account

* Account exists.
* Current status allows closure.
* The transition to `CLOSED` is valid.

### Closure Rules

The service must validate all applicable closure conditions, such as:

* Current balance requirements.
* Outstanding obligations.
* Pending operations.
* Product restrictions.
* Customer eligibility.
* Other domain-specific closure conditions.

---

## 31.3 Balance

If the business rule requires the balance to be zero:

```text
currentBalance == 0
```

must be satisfied before closure.

The service must not assume that the account can be closed simply because the user requested it.

---

## 31.4 Domain Behavior

Preferred:

```java
bankAccount.close();
```

The Domain Model must prevent invalid transitions.

---

## 31.5 Operation and Audit

A successful closure must generate:

```text
Operation
    operationType = ACCOUNT_CLOSURE
    performedBy = requestingUser
    affectedProduct = bankAccount
```

and the corresponding AuditLog.

---

# 32. Account Ownership Validation

Ownership validation is not merely an identifier comparison.

The business rule is:

```text
BankAccount.owner == Customer involved in operation
```

The service must use authoritative Domain information to establish this relationship.

A typical validation flow is:

```text
Input Customer
      |
      v
CustomerRepositoryPort
      |
      v
Authoritative Customer
      |
      v
Authoritative BankAccount
      |
      v
Validate ownership relationship
```

The reference `WithdrawFundsService` demonstrates this concept by retrieving the authoritative Customer and Bank Account before validating the ownership relationship.

---

# 33. Authorization

Authorization is operation-specific.

A customer User should generally be able to operate only on products belonging to the Customer associated with that User.

An employee User may operate according to their role and permissions.

The service must therefore distinguish:

```text
Identity
Authorization
Ownership
Product State
```

These are separate business conditions.

The service may perform all these validations within one cohesive operation.

---

# 34. Exception Model

The Bank Account subdomain may use exceptions such as:

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
UnauthorizedBankAccountOperationException
InvalidUserStatusException
```

Exceptions must communicate business failures clearly.

The implementation must not intentionally rely on technical exceptions such as:

```text
NullPointerException
SQL Exception
JPA Exception
Mongo Exception
```

to represent domain rule violations.

---

# 35. Persistence Boundary

The required architecture is:

```text
Application Service
        |
        v
Output Port
        |
        v
Persistence Adapter
        |
        v
Database
```

For Bank Accounts:

```text
BankAccountService
        |
        v
BankAccountRepositoryPort
        |
        v
BankAccountPersistenceAdapter
        |
        v
Database
```

For Customers:

```text
BankAccountService
        |
        v
CustomerRepositoryPort
        |
        v
CustomerPersistenceAdapter
        |
        v
Database
```

For Operations:

```text
BankAccountService
        |
        v
OperationRepositoryPort
        |
        v
OperationPersistenceAdapter
```

For Audit:

```text
BankAccountService
        |
        v
AuditRepositoryPort
        |
        v
AuditPersistenceAdapter
        |
        v
MongoDB
```

---

# 36. Persistence Entities

Persistence entities must never leave their persistence adapter.

Incorrect:

```text
Controller
    |
    v
Persistence Entity
    |
    v
Application Service
```

Correct:

```text
Controller
    |
    v
Request Mapper
    |
    v
Domain Model
    |
    v
Application Service
    |
    v
Output Port
    |
    v
Persistence Adapter
    |
    v
Persistence Entity
```

The adapter is responsible for mapping:

```text
Domain Model <-> Persistence Entity
```

---

# 37. Controller Responsibilities

Controllers are responsible only for transport concerns.

They may:

1. Receive external requests.
2. Perform transport-level validation.
3. Map request data to Domain Models and Value Objects.
4. Invoke an Input Port.
5. Map the Domain result to the external response.

Controllers must not implement:

* Account ownership validation.
* Customer eligibility rules.
* Withdrawal rules.
* Deposit rules.
* Balance mutation.
* Account status transitions.
* Authorization business decisions.
* Operation registration.
* Audit registration.
* Persistence.

---

# 38. Deposit Processing Flow

```text
Requesting User
       |
       v
Customer
       |
       v
BankAccount
       |
       v
Retrieve authoritative state
       |
       v
Validate User
       |
       v
Validate Customer
       |
       v
Validate ownership/access
       |
       v
Validate AccountStatus
       |
       v
Validate Money
       |
       v
Execute deposit
       |
       v
Persist BankAccount
       |
       v
Register Operation
       |
       v
Register Audit
```

---

# 39. Withdrawal Processing Flow

```text
Requesting User
       |
       v
Customer
       |
       v
BankAccount
       |
       v
Retrieve authoritative BankAccount
       |
       v
Validate BankAccount existence
       |
       v
Retrieve authoritative Customer
       |
       v
Validate Customer existence
       |
       v
Validate Customer ownership
       |
       v
Validate User
       |
       v
Validate authorization
       |
       v
Validate AccountStatus
       |
       v
Validate withdrawal amount
       |
       v
Validate sufficient balance
       |
       v
Execute withdrawal
       |
       v
Persist BankAccount
       |
       v
Register Operation
       |
       v
Register Audit
```

This is the reference flow for Bank Account state-changing services.

---

# 40. Blocking Processing Flow

```text
Requesting User
       |
       v
BankAccount
       |
       v
Retrieve authoritative state
       |
       v
Validate User
       |
       v
Validate authorization
       |
       v
Validate ownership/access
       |
       v
Validate current status
       |
       v
Validate status transition
       |
       v
Execute block()
       |
       v
Persist BankAccount
       |
       v
Register Operation
       |
       v
Register Audit
```

---

# 41. Consultation Flow

```text
Requesting User
       |
       v
Validate User
       |
       v
Validate Authorization
       |
       v
Retrieve BankAccount
       |
       v
Validate existence
       |
       v
Validate Customer relationship
       |
       v
Validate Product Access
       |
       v
Return Domain Model
```

Consultation operations do not normally modify Bank Account state.

However, if the business audit policy requires auditing sensitive consultations, an AuditLog must be generated.

---

# 42. Validation Matrix

The following matrix defines the minimum validation dimensions.

| Validation             |               Open |          Consult |          Balance |  Deposit | Withdraw |         Block |       Unblock |    Close |
| ---------------------- | -----------------: | ---------------: | ---------------: | -------: | -------: | ------------: | ------------: | -------: |
| User                   |                Yes |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| User status            |                Yes |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| Authorization          |                Yes |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| Customer               |                Yes |    When required |    When required |      Yes |      Yes | When required | When required |      Yes |
| Customer status        |                Yes |    When required |    When required |      Yes |      Yes | When required | When required |      Yes |
| Ownership/access       |                N/A |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| Account existence      |                N/A |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| Account status         |      Initial state |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| Operation rules        |                Yes |              Yes |              Yes |      Yes |      Yes |           Yes |           Yes |      Yes |
| Operation registration |                Yes |       Usually no |       Usually no |      Yes |      Yes |           Yes |           Yes |      Yes |
| Audit                  | Required by policy | Policy dependent | Policy dependent | Required | Required |      Required |      Required | Required |

`When required` means the validation depends on the business context of the operation.

---

# 43. Business Rules Summary

## BR-001 — Bank Account is a Domain Model

`BankAccount` must belong to the Domain Model.

---

## BR-002 — Bank Account Inherits BankingProduct

```text
BankingProduct
      |
      +-- BankAccount
```

---

## BR-003 — Owner is Customer

```text
BankAccount.owner : Customer
```

Ownership must not be modeled only as a primitive identifier.

---

## BR-004 — User and Customer are Different Concepts

A User represents the actor.

A Customer represents the banking customer.

---

## BR-005 — Product Ownership Must Be Verified

A Customer must not be allowed to operate on a Bank Account belonging to another Customer.

---

## BR-006 — User Authorization Must Be Verified

The existence of a User does not imply authorization.

---

## BR-007 — Customer Eligibility Must Be Verified

The existence of a Customer does not imply eligibility.

---

## BR-008 — Product State Must Be Authoritative

For state-changing operations, the service must use the current authoritative Bank Account state.

---

## BR-009 — Blocked Accounts Have Operational Restrictions

Operations prohibited by `BLOCKED` status must be rejected.

---

## BR-010 — Closed Accounts Have Operational Restrictions

Operations prohibited by `CLOSED` status must be rejected.

---

## BR-011 — Invalid Status Transitions Are Rejected

The Domain must prevent invalid transitions.

---

## BR-012 — Deposits Must Be Valid

Deposits must satisfy:

```text
amount > 0
```

and all other applicable monetary rules.

---

## BR-013 — Withdrawals Must Be Valid

Withdrawals must satisfy:

```text
amount > 0
amount <= currentBalance
```

and all other applicable monetary rules.

---

## BR-014 — Balance Must Be Changed Through Domain Behavior

The application service must use valid Domain behavior rather than unrestricted persistence-style setters.

---

## BR-015 — Significant Operations Generate Operation Records

State-changing Bank Account operations must generate an `Operation`.

---

## BR-016 — Relevant Operations Generate Audit Records

Operations subject to auditing must generate `AuditLog`.

---

## BR-017 — External Information Uses Output Ports

The application service must never directly access databases or infrastructure.

---

## BR-018 — Persistence Entities Stay in Adapters

Persistence entities must never cross into the application/domain layer.

---

## BR-019 — Controllers Do Not Implement Business Rules

Business rules belong to the Domain/Application layers.

---

## BR-020 — Services Must Be Cohesive

One application service may perform all validations required for its business operation.

The architecture must not be fragmented merely for the sake of creating smaller services.

---

# 44. Anti-Patterns

## 44.1 Direct Database Access

Invalid:

```java
EntityManager
JpaRepository
JdbcTemplate
MongoRepository
Connection
SQL
```

inside application services.

---

## 44.2 Primitive Application Contracts

Avoid:

```java
withdraw(
    String accountId,
    BigDecimal amount
);
```

when Domain Models and Value Objects represent those concepts.

---

## 44.3 Trusting Caller-Supplied State

Do not assume the caller-provided Bank Account contains the current:

* Balance.
* Status.
* Owner.
* Other persisted business state.

---

## 44.4 Missing Ownership Validation

Invalid:

```text
Customer exists
+
BankAccount exists
=
Customer may operate on BankAccount
```

Ownership must be explicitly established.

---

## 44.5 Confusing User and Customer

Invalid:

```text
User == Customer
```

A User may be associated with a Customer, but they are different domain concepts.

---

## 44.6 Business Rules in Controllers

Invalid:

```text
Controller
 |
 +-- check balance
 +-- check ownership
 +-- change status
 +-- modify balance
```

---

## 44.7 Direct Balance Mutation

Avoid:

```java
bankAccount.setCurrentBalance(
    bankAccount.getCurrentBalance().subtract(amount)
);
```

as the primary domain behavior.

Prefer:

```java
bankAccount.withdraw(money);
```

---

## 44.8 Excessive Service Fragmentation

Avoid:

```text
WithdrawFundsService
       |
       +-- ValidateUserService
       +-- ValidateCustomerService
       +-- ValidateOwnershipService
       +-- ValidateAccountStatusService
       +-- ValidateBalanceService
       +-- ValidateAmountService
```

when these validations are only parts of the same withdrawal business operation.

Prefer:

```text
WithdrawFundsService
       |
       +-- validateUser()
       +-- validateCustomer()
       +-- validateOwnership()
       +-- validateAccount()
       +-- validateWithdrawal()
       +-- executeWithdrawal()
```

This provides cohesion without unnecessary architectural fragmentation.

---

# 45. Testing Requirements

The Bank Account subdomain must be testable without infrastructure.

Application services must be testable using mocks, fakes, or stubs for Output Ports.

Tests must not require:

```text
MySQL
MongoDB
REST
JPA
Hibernate
real persistence adapters
```

---

## 45.1 User Tests

Test:

* Valid User.
* Missing User.
* Inactive User.
* Unauthorized User.
* Authorized Customer User.
* Authorized Employee User.

---

## 45.2 Customer Tests

Test:

* Customer exists.
* Customer does not exist.
* Customer inactive.
* Customer not eligible.
* Customer does not own the Bank Account.

---

## 45.3 Bank Account Tests

Test:

* Account exists.
* Account does not exist.
* ACTIVE account.
* BLOCKED account.
* CLOSED account.
* Invalid status transition.

---

## 45.4 Deposit Tests

Test:

* Valid deposit.
* Zero amount.
* Negative amount.
* Invalid currency.
* Deposit into BLOCKED account.
* Deposit into CLOSED account.
* Unauthorized deposit.
* Deposit into another customer's account.

---

## 45.5 Withdrawal Tests

Test:

* Valid withdrawal.
* Zero amount.
* Negative amount.
* Invalid currency.
* Insufficient balance.
* Withdrawal from BLOCKED account.
* Withdrawal from CLOSED account.
* Unauthorized withdrawal.
* Withdrawal from another customer's account.

---

## 45.6 Blocking Tests

Test:

* ACTIVE -> BLOCKED.
* BLOCKED -> BLOCKED rejected.
* CLOSED -> BLOCKED rejected.
* Unauthorized blocking.

---

## 45.7 Unblocking Tests

Test:

* BLOCKED -> ACTIVE.
* ACTIVE -> ACTIVE rejected.
* CLOSED -> ACTIVE rejected.
* Unauthorized unblocking.

---

## 45.8 Closing Tests

Test:

* Valid closure.
* Already closed account.
* Invalid status.
* Non-zero balance when zero balance is required.
* Outstanding obligations.
* Unauthorized closure.

---

## 45.9 Operation and Audit Tests

For every applicable state-changing operation, verify:

```text
Operation generated
Correct operation type
Correct requesting User
Correct affected BankAccount
Correct execution date
Correct operation details
AuditLog generated
```

---

# 46. Definition of Done

A Bank Account service is complete only when:

* It represents a coherent business operation.
* It receives appropriate Domain Models and Value Objects.
* It does not expose REST DTOs as application contracts.
* It does not receive persistence entities.
* It does not directly depend on persistence technology.
* It retrieves authoritative state when required.
* It validates the requesting User.
* It validates User status.
* It validates authorization.
* It validates the Customer when applicable.
* It validates Customer status and eligibility when applicable.
* It validates Customer-BankAccount ownership when applicable.
* It validates Bank Account existence.
* It validates Bank Account status.
* It validates all operation-specific business rules.
* It executes valid Domain behavior.
* It persists through Output Ports.
* It generates the required Operation.
* It generates the required AuditLog.
* It maintains transactional consistency.
* It protects Domain invariants.
* It can be tested without infrastructure.

---

# 47. Final Service Catalog

The Bank Account Management subdomain contains the following application services:

```text
Bank Account Management
|
+-- Open Bank Account
|
+-- Consult Bank Account
|
+-- Consult Account Balance
|
+-- Deposit Funds
|
+-- Withdraw Funds
|
+-- Block Bank Account
|
+-- Unblock Bank Account
|
+-- Close Bank Account
```

Ownership validation, User validation, Customer validation, authorization validation, status validation, and operation-specific validation are **business responsibilities of the applicable service**.

They do not need to become independent application services simply because they are logically identifiable validations.

---

# 48. Reference Architecture

The final architecture for a state-changing Bank Account operation should follow:

```text
                         INPUT
                           |
                           v
                  +------------------+
                  |   Input Adapter  |
                  |    Controller    |
                  +--------+---------+
                           |
                           | Domain Models / Value Objects
                           v
                  +------------------+
                  |   Input Port     |
                  +--------+---------+
                           |
                           v
                  +------------------+
                  | Application      |
                  |     Service      |
                  |                  |
                  | - User validation|
                  | - Customer       |
                  | - Authorization  |
                  | - Ownership      |
                  | - Product state  |
                  | - Business rules |
                  | - Domain action  |
                  +--------+---------+
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
      BankAccount     Customer      Operation/Audit
      Repository     Repository      Registration
             |             |             |
             v             v             v
          Adapter       Adapter       Output Ports
             |             |             |
             +-------------+-------------+
                           |
                           v
                     Infrastructure
```

The essential architectural boundary is:

```text
Domain/Application
        |
        | Output Ports
        v
Infrastructure Adapters
```

Never:

```text
Domain/Application
        |
        v
Database
```

---

# 49. Final Design Rule

The Bank Account Management subdomain must follow this principle:

> **Each application service is a cohesive business operation responsible for determining whether the operation can be executed, validating all required User, Customer, ownership, authorization, product-state, and operation-specific business conditions, executing valid Domain behavior, persisting through Output Ports, and generating the required Operation and Audit records.**

The design should therefore avoid both insufficient responsibility and excessive fragmentation.

### Insufficient

```text
find BankAccount
      |
      v
change balance
```

### Excessively fragmented

```text
ValidateUserService
        |
ValidateCustomerService
        |
ValidateOwnershipService
        |
ValidateProductService
        |
ValidateBalanceService
        |
ExecuteOperationService
```

### Preferred

```text
WithdrawFundsService
|
+-- Retrieve authoritative state
+-- Validate User
+-- Validate Customer
+-- Validate authorization
+-- Validate ownership
+-- Validate BankAccount
+-- Validate AccountStatus
+-- Validate withdrawal rules
+-- Execute Domain behavior
+-- Persist BankAccount
+-- Register Operation
+-- Register Audit
```

This pattern is the reference for the remaining Bank Account services.

The objective is not to minimize the number of methods or services.

The objective is to guarantee **business correctness while preserving DDD boundaries, Hexagonal Architecture, domain independence, cohesion, testability, and infrastructure isolation**.
