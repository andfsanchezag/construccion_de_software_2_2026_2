# Domain Model

## Introduction

The Domain Model represents the core business entities of the Banking Information Management System. These entities encapsulate the business rules, data, relationships, and lifecycle concepts described in the system specification.

The model follows Object-Oriented Design and Domain-Driven Design (DDD) principles. Inheritance is used to represent genuine domain specialization, while explicit object relationships are preferred over generic identifier fields.

The model distinguishes between:

* **Persons**, which represent identifiable people and their role within the system.
* **Customers**, which represent people or organizations that have a banking relationship with the institution.
* **Users**, which represent system identities used for authentication and authorization.
* **Banking Products**, which represent financial products and banking services.
* **Operations**, which represent significant business actions performed on banking products or services.
* **Audit Logs**, which provide an immutable historical record of operations.

A banking product or service may generate multiple operations throughout its lifecycle. Every significant business operation must be recorded in the audit trail.

---

# Domain Class Hierarchy

```text
Person (Abstract)
├── Customer (Abstract)
│   ├── NaturalCustomer
│   └── BusinessCustomer
│
└── User

BankingProduct (Abstract)
├── BankAccount
├── Loan
└── Transfer

Operation

AuditLog
```

---

# Domain Relationships

```text
Person
   │
   ├── Customer
   │      ├── NaturalCustomer
   │      └── BusinessCustomer
   │
   └── User
          │
          └── customer : Customer (optional)

Customer
   │
   ├── owns ───────────────> BankAccount
   │
   └── requests/owns ──────> Loan

BankingProduct
   │
   └── generates ──────────> Operation
                                │
                                └── recorded in ──────> AuditLog

BankAccount
   ├── sourceAccount ──────> Transfer
   └── destinationAccount ─> Transfer

Loan
   └── destinationAccount ─> BankAccount

Transfer
   ├── createdBy ──────────> User
   └── approvedBy ────────> User
```

---

# Entities

---

# Person (Abstract)

## Description

Represents any identifiable person within the banking system.

This abstract class centralizes the common identity and contact information shared by customers and system users.

The role assigned to a person represents what that person means within the system and determines the responsibilities or business capabilities associated with that person.

This class cannot be instantiated directly.

## Attributes

| Attribute      | Type       | Description                                                                                                                                                                     |
| -------------- | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| identification | String     | Unique identifier of the person. For natural persons, represents the national identification number. For business customers, represents the business tax identification number. |
| name           | String     | Full name of the natural person or legal name of the business.                                                                                                                  |
| email          | String     | Primary registered email address.                                                                                                                                               |
| phoneNumber    | String     | Primary contact phone number.                                                                                                                                                   |
| address        | String     | Registered residential or business address.                                                                                                                                     |
| role           | SystemRole | Business role that defines the person's responsibilities or participation within the system.                                                                                    |

## Relationships

* A `Person` may be specialized as a `Customer`.
* A `Person` may be represented as a `User` for system access.
* The `role` belongs to `Person` because it represents the person's meaning and responsibilities within the banking system.

---

# Customer (Abstract)

## Description

Represents any customer of the banking institution.

A customer has a business relationship with the bank and may own or use banking products and services.

Customer status is independent from system user status. A customer may remain an active banking customer even when an associated system user is inactive or blocked.

This class specializes the `Person` entity.

This class cannot be instantiated directly.

## Attributes

| Attribute | Type           | Description                                                                |
| --------- | -------------- | -------------------------------------------------------------------------- |
| status    | CustomerStatus | Current operational status of the customer within the banking institution. |

## Relationships

* A customer may own one or more `BankAccount` instances.
* A customer may request or own one or more `Loan` instances.
* A customer may be associated with one or more `User` instances when system access is required.

---

# NaturalCustomer

## Description

Represents an individual customer of the bank.

Natural customers may own accounts, request loans, and participate in banking operations.

A natural customer must be at least 18 years old.

## Inherits From

`Customer`

## Attributes

| Attribute | Type      | Description                                                           |
| --------- | --------- | --------------------------------------------------------------------- |
| birthDate | LocalDate | Customer's date of birth. The customer must be at least 18 years old. |

---

# BusinessCustomer

## Description

Represents a legal business entity registered as a customer of the bank.

Business customers may own corporate banking products and authorize operational users.

## Inherits From

`Customer`

## Attributes

| Attribute           | Type            | Description                                                                 |
| ------------------- | --------------- | --------------------------------------------------------------------------- |
| legalRepresentative | NaturalCustomer | Natural person legally authorized to represent the company before the bank. |

## Relationships

* A business customer is represented by a `NaturalCustomer` acting as its legal representative.
* Business customers may have users associated with them for operational purposes.

---

# User

## Description

Represents a system identity used for authentication and authorization.

A user inherits the identity and role information defined by `Person`.

A user may be associated with a customer when the user acts on behalf of that customer. Internal users, such as bank employees, may exist without an associated customer.

User status is independent from customer status.

This class represents access to the banking system and is not a replacement for the `Customer` entity.

## Inherits From

`Person`

## Attributes

| Attribute | Type       | Description                                                                                             |
| --------- | ---------- | ------------------------------------------------------------------------------------------------------- |
| userId    | Integer    | Internal unique identifier of the system user.                                                          |
| username  | String     | Login name used during authentication.                                                                  |
| password  | String     | Secure password hash stored by the system.                                                              |
| status    | UserStatus | Current status of the user's system access.                                                             |
| customer  | Customer?  | Customer represented by the user when applicable. Internal users may not be associated with a customer. |

## Relationships

* A `User` may be associated with zero or one `Customer`.
* A `User` may create banking operations.
* A `User` may approve banking operations when authorized by its role.

---

# BankingProduct (Abstract)

## Description

Represents any banking product or service offered or managed by the banking institution.

Banking products and services are the business capabilities through which customers interact with the bank.

Each banking product has a unique identifier and may generate multiple business operations throughout its lifecycle.

Every significant business action performed on a banking product or service must generate an `Operation`, which must subsequently be recorded in the audit trail.

This class cannot be instantiated directly.

## Attributes

| Attribute  | Type   | Description                                          |
| ---------- | ------ | ---------------------------------------------------- |
| identifier | String | Unique identifier of the banking product or service. |

## Relationships

* A `BankingProduct` may generate zero or more `Operation` instances.
* Each `Operation` references the affected `BankingProduct`.
* A banking product may be associated with a customer depending on its concrete specialization.

## Business Rule

```text
Every significant business action performed on a BankingProduct
must generate an Operation.

Every Operation must be recorded in the AuditLog.
```

---

# BankAccount

## Description

Represents a bank account managed by the institution.

A bank account is a banking product owned by a customer. Accounts store balances and participate in deposits, withdrawals, transfers, and other banking operations.

Changes to the account lifecycle or significant account movements generate operations that must be recorded in the audit trail.

## Inherits From

`BankingProduct`

## Attributes

| Attribute      | Type          | Description                                |
| -------------- | ------------- | ------------------------------------------ |
| accountType    | AccountType   | Type of bank account.                      |
| owner          | Customer      | Customer who owns the account.             |
| currentBalance | BigDecimal    | Available account balance.                 |
| currency       | Currency      | Currency in which the account operates.    |
| accountStatus  | AccountStatus | Current operational status of the account. |
| openingDate    | LocalDate     | Date when the account was created.         |

## Relationships

* A `BankAccount` belongs to one `Customer`.
* A `BankAccount` may be the source account of multiple `Transfer` instances.
* A `BankAccount` may be the destination account of multiple `Transfer` instances.
* A `BankAccount` may generate multiple `Operation` instances.

## Examples of Generated Operations

* `ACCOUNT_OPENING`
* `DEPOSIT`
* `WITHDRAWAL`
* `TRANSFER`
* `ACCOUNT_BLOCKING`
* `ACCOUNT_UNBLOCKING`
* `ACCOUNT_CLOSING`

---

# Loan

## Description

Represents a credit product requested and managed by a customer.

Loans follow a lifecycle composed of application, review, approval or rejection, disbursement, and closure.

Each significant action or state-changing event related to a loan generates an operation that must be recorded in the audit trail.

## Inherits From

`BankingProduct`

## Attributes

| Attribute          | Type        | Description                             |
| ------------------ | ----------- | --------------------------------------- |
| applicant          | Customer    | Customer requesting the loan.           |
| loanType           | LoanType    | Classification of the loan product.     |
| requestedAmount    | BigDecimal  | Amount requested by the customer.       |
| approvedAmount     | BigDecimal  | Amount approved by the bank.            |
| interestRate       | BigDecimal  | Annual interest rate.                   |
| termInMonths       | Integer     | Loan duration expressed in months.      |
| loanStatus         | LoanStatus  | Current state of the loan.              |
| approvalDate       | LocalDate   | Date on which the loan was approved.    |
| disbursementDate   | LocalDate   | Date on which the funds were disbursed. |
| destinationAccount | BankAccount | Account receiving the approved funds.   |

## Relationships

* A `Loan` is requested by one `Customer`.
* A `Loan` may be disbursed to one `BankAccount`.
* A `Loan` may generate multiple `Operation` instances.

## Examples of Generated Operations

* `LOAN_APPLICATION`
* `LOAN_APPROVAL`
* `LOAN_REJECTION`
* `LOAN_DISBURSEMENT`
* `LOAN_PAYMENT`
* `LOAN_OVERDUE`
* `LOAN_CANCELLATION`

---

# Transfer

## Description

Represents a banking service that moves funds between two bank accounts.

Transfers are considered banking products or services because they represent a service offered by the institution and participate in the banking product lifecycle.

A transfer may require approval depending on the applicable business rules.

Every significant action in the transfer lifecycle generates an operation that must be recorded in the audit trail.

## Inherits From

`BankingProduct`

## Attributes

| Attribute          | Type           | Description                                               |
| ------------------ | -------------- | --------------------------------------------------------- |
| sourceAccount      | BankAccount    | Account from which the funds are debited.                 |
| destinationAccount | BankAccount    | Account receiving the transferred funds.                  |
| amount             | BigDecimal     | Amount to be transferred.                                 |
| creationDate       | LocalDateTime  | Date and time when the transfer was created.              |
| approvalDate       | LocalDateTime  | Date and time when the transfer was approved.             |
| transferStatus     | TransferStatus | Current execution status of the transfer.                 |
| createdBy          | User           | User who created the transfer request.                    |
| approvedBy         | User?          | User who approved the transfer when approval is required. |

## Relationships

* A `Transfer` has one source `BankAccount`.
* A `Transfer` has one destination `BankAccount`.
* A `Transfer` is created by one `User`.
* A `Transfer` may be approved by one `User`.
* A `Transfer` may generate multiple `Operation` instances.

## Examples of Generated Operations

* `TRANSFER_CREATION`
* `TRANSFER_APPROVAL`
* `TRANSFER_REJECTION`
* `TRANSFER_EXECUTION`
* `TRANSFER_EXPIRATION`

---

# Operation

## Description

Represents a significant business action executed over a banking product or service.

Operations provide traceability between users, banking products, and audit records.

A banking product may generate multiple operations during its lifecycle.

An operation represents an event or action that occurred; it is distinct from the current status of the affected product.

For example:

```text
BankAccount.accountStatus = BLOCKED
```

represents the current state of the account, while:

```text
Operation.operationType = ACCOUNT_BLOCKING
```

represents the action that caused the state change.

## Attributes

| Attribute       | Type           | Description                                           |
| --------------- | -------------- | ----------------------------------------------------- |
| operationId     | Integer        | Unique operation identifier.                          |
| operationType   | OperationType  | Category of the business operation.                   |
| executionDate   | LocalDateTime  | Date and time when the operation occurred.            |
| performedBy     | User           | User responsible for executing the operation.         |
| affectedProduct | BankingProduct | Banking product or service affected by the operation. |

## Relationships

* One `BankingProduct` may generate zero or more `Operation` instances.
* Each `Operation` affects one `BankingProduct`.
* Each `Operation` is performed by one `User`.
* Each significant `Operation` must be recorded in the `AuditLog`.

---

# AuditLog

## Description

Represents the immutable audit trail of the banking system.

Each audit record stores historical information about a significant business operation.

Audit records are intended to be persisted in a NoSQL database to support flexible operation-specific details and historical traceability.

Audit records are append-only and must not be modified or deleted after being persisted.

## Attributes

| Attribute       | Type                | Description                                                  |
| --------------- | ------------------- | ------------------------------------------------------------ |
| auditId         | String              | Unique identifier of the audit record.                       |
| operationType   | OperationType       | Type of business operation recorded.                         |
| operationDate   | LocalDateTime       | Timestamp when the event occurred.                           |
| performedBy     | User                | User responsible for the operation.                          |
| userRole        | SystemRole          | Role of the user at the time of execution.                   |
| affectedProduct | BankingProduct      | Banking product or service involved in the operation.        |
| details         | Map<String, Object> | Flexible document containing operation-specific information. |

## Business Rules

* Audit records are immutable.
* Audit records are append-only.
* An audit record cannot be deleted after persistence.
* Every significant business operation must produce an audit record.
* The `userRole` must represent the role applicable at the time the operation was performed.
* The `details` field may contain operation-specific information required for traceability.

---

# Domain Lifecycle Relationship

The general lifecycle of a banking product or service is:

```text
BankingProduct
      │
      │ significant business action
      ▼
  Operation
      │
      │ audit registration
      ▼
  AuditLog
```

For example, when a bank account is blocked:

```text
BankAccount
    │
    │ accountStatus changes
    ▼
BLOCKED
    │
    ├── Operation
    │      operationType = ACCOUNT_BLOCKING
    │      performedBy = User
    │
    └── AuditLog
           operationType = ACCOUNT_BLOCKING
           affectedProduct = BankAccount
           performedBy = User
           details = operation-specific information
```

Similarly, when a loan is approved:

```text
Loan
    │
    │ loanStatus changes
    ▼
APPROVED
    │
    ├── Operation
    │      operationType = LOAN_APPROVAL
    │      performedBy = User
    │
    └── AuditLog
           operationType = LOAN_APPROVAL
           affectedProduct = Loan
           performedBy = User
           details = operation-specific information
```

---

# Domain Value Objects

## Introduction

Value Objects represent immutable concepts within the banking domain.

Unlike entities, they do not have their own identity. They are defined by their values and encapsulate controlled business concepts.

Value Objects prevent primitive values and scattered string literals from being used throughout the domain.

---

# Value Object Hierarchy

```text
DomainCatalog (Abstract)
├── SystemRole
├── CustomerStatus
├── UserStatus
├── AccountStatus
├── LoanStatus
├── TransferStatus
├── AccountType
├── LoanType
├── OperationType
└── Currency
```

---

# DomainCatalog (Abstract)

## Description

Represents a generic business catalog used throughout the banking domain.

All controlled business values that require a business code, display name, and description inherit from this abstraction.

## Attributes

| Attribute   | Type   | Description                                           |
| ----------- | ------ | ----------------------------------------------------- |
| code        | String | Unique business identifier.                           |
| name        | String | Human-readable name displayed within the application. |
| description | String | Business definition of the catalog value.             |

## Characteristics

* Immutable.
* Equality is based on catalog values.
* Catalog values must be controlled by the domain.
* Catalog values must not be represented by arbitrary strings throughout the application.

---

# SystemRole

## Description

Represents the responsibilities and permissions assigned to a person within the banking system.

The role is defined at the `Person` level because it represents what the person means within the system.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                | Name                | Description                                          |
| ------------------- | ------------------- | ---------------------------------------------------- |
| NATURAL_CUSTOMER    | Natural Customer    | Individual banking customer.                         |
| BUSINESS_CUSTOMER   | Business Customer   | Corporate banking customer.                          |
| TELLER_EMPLOYEE     | Teller Employee     | Performs branch operations.                          |
| COMMERCIAL_EMPLOYEE | Commercial Employee | Manages customer relationships and loan requests.    |
| BUSINESS_OPERATOR   | Business Operator   | Performs operations on behalf of business customers. |
| BUSINESS_SUPERVISOR | Business Supervisor | Approves business transfers requiring authorization. |
| INTERNAL_ANALYST    | Internal Analyst    | Reviews and approves loan applications.              |

---

# CustomerStatus

## Description

Represents the operational status of a customer within the banking institution.

Customer status is independent from system access status.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                                                |
| -------- | -------- | -------------------------------------------------------------------------- |
| ACTIVE   | Active   | Customer maintains an active banking relationship.                         |
| INACTIVE | Inactive | Customer exists but is not currently active for normal banking operations. |
| BLOCKED  | Blocked  | Customer's banking relationship has been suspended.                        |

---

# UserStatus

## Description

Represents the status of a user's access to the banking system.

User status is independent from the status of the associated customer.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                       |
| -------- | -------- | ------------------------------------------------- |
| ACTIVE   | Active   | User can access the system normally.              |
| INACTIVE | Inactive | User exists but cannot perform system operations. |
| BLOCKED  | Blocked  | User access has been suspended.                   |

---

# AccountStatus

## Description

Represents the operational state of a bank account.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code    | Name    | Description                            |
| ------- | ------- | -------------------------------------- |
| ACTIVE  | Active  | Account is fully operational.          |
| BLOCKED | Blocked | Transactions are temporarily disabled. |
| CLOSED  | Closed  | Account has been permanently closed.   |

---

# LoanStatus

## Description

Represents the lifecycle of a loan.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code         | Name         | Description                           |
| ------------ | ------------ | ------------------------------------- |
| UNDER_REVIEW | Under Review | Loan request is under evaluation.     |
| APPROVED     | Approved     | Loan has been approved.               |
| REJECTED     | Rejected     | Loan request was rejected.            |
| DISBURSED    | Disbursed    | Approved funds have been transferred. |
| CLOSED       | Closed       | Loan has been fully settled.          |

---

# TransferStatus

## Description

Represents the execution state of a transfer service.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                 | Name                 | Description                                            |
| -------------------- | -------------------- | ------------------------------------------------------ |
| PENDING              | Pending              | Transfer has been created.                             |
| WAITING_FOR_APPROVAL | Waiting for Approval | Transfer requires managerial approval.                 |
| APPROVED             | Approved             | Transfer has been approved and is ready for execution. |
| EXECUTED             | Executed             | Funds have been successfully transferred.              |
| REJECTED             | Rejected             | Transfer request has been denied.                      |
| EXPIRED              | Expired              | Approval time window has expired.                      |

---

# AccountType

## Description

Represents the different bank account products offered by the institution.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name             | Description                                           |
| -------- | ---------------- | ----------------------------------------------------- |
| SAVINGS  | Savings Account  | Standard interest-bearing deposit account.            |
| CHECKING | Checking Account | Transaction account intended for frequent operations. |
| BUSINESS | Business Account | Account designed for corporate customers.             |

---

# LoanType

## Description

Represents the different credit products provided by the bank.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name          | Description                             |
| -------- | ------------- | --------------------------------------- |
| PERSONAL | Personal Loan | Loan intended for personal use.         |
| MORTGAGE | Mortgage Loan | Loan secured by real estate.            |
| VEHICLE  | Vehicle Loan  | Loan used to finance vehicle purchases. |
| BUSINESS | Business Loan | Loan intended for business financing.   |

---

# OperationType

## Description

Represents the type of significant business operation executed within the banking system.

Every significant operation generated by a banking product or service must reference one operation type.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                | Name                | Description                                     |
| ------------------- | ------------------- | ----------------------------------------------- |
| ACCOUNT_OPENING     | Account Opening     | Creation of a new bank account.                 |
| DEPOSIT             | Deposit             | Deposit of funds into an account.               |
| WITHDRAWAL          | Withdrawal          | Withdrawal of funds from an account.            |
| ACCOUNT_BLOCKING    | Account Blocking    | Blocking of a bank account.                     |
| ACCOUNT_UNBLOCKING  | Account Unblocking  | Removal of an account block.                    |
| ACCOUNT_CLOSING     | Account Closing     | Permanent closure of a bank account.            |
| TRANSFER_CREATION   | Transfer Creation   | Creation of a transfer request.                 |
| TRANSFER_APPROVAL   | Transfer Approval   | Approval of a transfer requiring authorization. |
| TRANSFER_REJECTION  | Transfer Rejection  | Rejection of a transfer request.                |
| TRANSFER_EXECUTION  | Transfer Execution  | Successful execution of a transfer.             |
| TRANSFER_EXPIRATION | Transfer Expiration | Expiration of the transfer approval window.     |
| LOAN_APPLICATION    | Loan Application    | Submission of a loan request.                   |
| LOAN_APPROVAL       | Loan Approval       | Approval of a loan request.                     |
| LOAN_REJECTION      | Loan Rejection      | Rejection of a loan request.                    |
| LOAN_DISBURSEMENT   | Loan Disbursement   | Transfer of approved loan funds.                |
| LOAN_PAYMENT        | Loan Payment        | Registration of a loan payment.                 |
| LOAN_CLOSING        | Loan Closing        | Completion and closure of a loan.               |

---

# Currency

## Description

Represents the monetary currency supported by banking products.

## Inherits From

`DomainCatalog`

## Additional Attributes

| Attribute | Type   | Description             |
| --------- | ------ | ----------------------- |
| isoCode   | String | ISO 4217 currency code. |
| symbol    | String | Currency symbol.        |

## Allowed Values

| ISO Code | Name                 | Symbol |
| -------- | -------------------- | ------ |
| COP      | Colombian Peso       | $      |
| USD      | United States Dollar | $      |
| EUR      | Euro                 | €      |

---

# Primitive Enumerations

The following concepts are simple enumerations because they represent fixed technical values without requiring business catalog metadata or domain identity.

---

## ApprovalDecision

### Description

Represents the result of an approval process.

### Values

* APPROVED
* REJECTED

---

## NotificationChannel

### Description

Represents the communication channel used by the system.

### Values

* EMAIL
* SMS
* PUSH_NOTIFICATION

---

## AuditSeverity

### Description

Represents the severity level of an audit event.

### Values

* INFORMATION
* WARNING
* ERROR
* CRITICAL

---

# Domain Design Rules

## Person and User

* `User` inherits from `Person`.
* `role` is defined in `Person` and inherited by `Customer` and `User`.
* `User` must not duplicate the `role` attribute.
* `User.customer` is an explicit domain relationship and must not be represented through a generic identifier such as `relatedEntityId`.
* A `User` may exist without an associated `Customer` when representing an internal employee.

## Customer and User Status

* `CustomerStatus` represents the customer's banking relationship.
* `UserStatus` represents system access.
* These statuses are independent concepts even when they currently contain similar values.
* A change in customer status does not automatically imply a change in user status unless explicitly required by a business rule.

## Banking Products and Services

* `BankingProduct` represents both financial products and banking services.
* `BankAccount`, `Loan`, and `Transfer` are specializations of `BankingProduct`.
* `identifier` is the common identity attribute of all banking products and services.
* Attributes are inherited only when they represent the same business concept across the parent and child classes.
* Product-specific lifecycle states remain in their corresponding status value objects.

## Operations

* A banking product or service may generate multiple operations throughout its lifecycle.
* Significant business actions must generate an `Operation`.
* An operation references the affected `BankingProduct`.
* An operation references the `User` who performed the action.
* Product status represents the current state; operation represents an event or action that occurred.

## Audit Trail

* Every significant operation must be recorded in the `AuditLog`.
* Audit records are immutable.
* Audit records are append-only.
* Audit records must preserve the user role applicable when the operation was performed.
* Operation-specific information may be stored in the flexible `details` document.

## Value Objects

* Value Objects are immutable.
* Equality is determined by their values rather than object identity.
* Business entities reference Value Objects instead of primitive strings for controlled business concepts.
* Primitive enumerations are reserved for fixed technical concepts that do not require business metadata or behavior.
* This approach improves domain expressiveness, consistency, maintainability, and alignment with Domain-Driven Design principles.
