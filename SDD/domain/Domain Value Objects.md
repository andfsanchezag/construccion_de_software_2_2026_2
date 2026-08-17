# Domain Value Objects

## Introduction

Value Objects represent immutable concepts within the banking domain.

Unlike Entities, Value Objects do not have their own identity. They are defined entirely by their values and are used to encapsulate controlled business concepts, improve domain expressiveness, and prevent the use of primitive values or scattered string literals throughout the application.

The banking domain uses Value Objects for business catalogs such as roles, statuses, product types, loan types, operation types, and currencies.

All business catalogs inherit from `DomainCatalog`.

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

`DomainCatalog` provides a consistent structure for controlled business values that require a code, human-readable name, and business description.

This class cannot be instantiated directly.

## Attributes

| Attribute   | Type   | Description                                           |
| ----------- | ------ | ----------------------------------------------------- |
| code        | String | Unique business identifier of the catalog value.      |
| name        | String | Human-readable name displayed within the application. |
| description | String | Business definition of the catalog value.             |

## Characteristics

* Immutable.
* Equality is determined by value rather than object identity.
* Catalog values are controlled by the domain.
* Catalog values must not be represented by arbitrary strings throughout the application.
* Each catalog value must have a unique `code`.

---

# SystemRole

## Description

Represents the responsibilities and permissions assigned to a person within the banking system.

The role is a characteristic of `Person` because it represents what the person means within the system and the responsibilities associated with that person.

The `role` attribute is therefore defined in `Person` and inherited by its specializations, including `Customer` and `User`.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                | Name                | Description                                                                  |
| ------------------- | ------------------- | ---------------------------------------------------------------------------- |
| NATURAL_CUSTOMER    | Natural Customer    | Individual banking customer.                                                 |
| BUSINESS_CUSTOMER   | Business Customer   | Corporate banking customer.                                                  |
| TELLER_EMPLOYEE     | Teller Employee     | Employee responsible for performing branch operations.                       |
| COMMERCIAL_EMPLOYEE | Commercial Employee | Employee responsible for customer relationships and loan-related activities. |
| BUSINESS_OPERATOR   | Business Operator   | User authorized to perform operations on behalf of business customers.       |
| BUSINESS_SUPERVISOR | Business Supervisor | User authorized to approve business transfers requiring authorization.       |
| INTERNAL_ANALYST    | Internal Analyst    | User responsible for reviewing and approving loan applications.              |

---

# CustomerStatus

## Description

Represents the current operational status of a customer within the banking institution.

`CustomerStatus` is independent from `UserStatus`. It represents the state of the customer's banking relationship rather than the state of the customer's system access.

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

Represents the current status of a user's access to the banking system.

`UserStatus` is independent from `CustomerStatus`. A user may be blocked or inactive while the associated customer remains active.

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

The status describes the current lifecycle state of the account and is independent from the status of its owner or associated system user.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code    | Name    | Description                                                           |
| ------- | ------- | --------------------------------------------------------------------- |
| ACTIVE  | Active  | Account is fully operational and may perform authorized transactions. |
| BLOCKED | Blocked | Transactions are temporarily disabled.                                |
| CLOSED  | Closed  | Account has been permanently closed.                                  |

---

# LoanStatus

## Description

Represents the lifecycle state of a loan.

The status changes as the loan moves through the application, approval, rejection, disbursement, and closure processes.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code         | Name         | Description                                                      |
| ------------ | ------------ | ---------------------------------------------------------------- |
| UNDER_REVIEW | Under Review | Loan request is under evaluation.                                |
| APPROVED     | Approved     | Loan has been approved but funds have not yet been disbursed.    |
| REJECTED     | Rejected     | Loan request has been rejected.                                  |
| DISBURSED    | Disbursed    | Approved funds have been transferred to the destination account. |
| OVERDUE      | Overdue      | Loan has active obligations that have not been met on time.      |
| CANCELLED    | Cancelled    | Loan has been cancelled and is no longer active.                 |

## Lifecycle

```text
UNDER_REVIEW
      │
      ├──────────────> REJECTED
      │
      ├──────────────> CANCELLED
      │
      ▼
  APPROVED
      │
      ├──────────────> CANCELLED
      │
      ▼
 DISBURSED
      │
      ▼
  OVERDUE
      │
      ▼
 CANCELLED
```

---

# TransferStatus

## Description

Represents the execution state of a transfer service.

The status describes the current state of a transfer while its operations provide the historical record of the actions performed throughout its lifecycle.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                 | Name                 | Description                                                           |
| -------------------- | -------------------- | --------------------------------------------------------------------- |
| PENDING              | Pending              | Transfer has been created and is pending processing.                  |
| WAITING_FOR_APPROVAL | Waiting for Approval | Transfer requires managerial or authorized approval before execution. |
| APPROVED             | Approved             | Transfer has been approved and is ready for execution.                |
| EXECUTED             | Executed             | Funds have been successfully transferred.                             |
| REJECTED             | Rejected             | Transfer request has been denied.                                     |
| EXPIRED              | Expired              | The approval or execution time window has expired.                    |

## Lifecycle

```text
PENDING
   │
   ├──────────────> REJECTED
   │
   ▼
WAITING_FOR_APPROVAL
   │
   ├──────────────> REJECTED
   │
   ├──────────────> EXPIRED
   │
   ▼
APPROVED
   │
   ▼
EXECUTED
```

---

# AccountType

## Description

Represents the different types of bank accounts offered by the institution.

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

Operations represent business events or actions performed over banking products and services.

Every significant operation generated by a `BankingProduct` must reference an `OperationType`.

Operations are independent from product statuses:

* A **status** represents the current state of an entity.
* An **operation** represents an action or event that occurred.

For example:

```text
BankAccount.accountStatus = BLOCKED
```

represents the current state of the account, while:

```text
Operation.operationType = ACCOUNT_BLOCKING
```

represents the event that caused the account to become blocked.

## Inherits From

`DomainCatalog`

## Allowed Values

### Account Operations

| Code               | Name               | Description                             |
| ------------------ | ------------------ | --------------------------------------- |
| ACCOUNT_OPENING    | Account Opening    | Creation of a new bank account.         |
| DEPOSIT            | Deposit            | Deposit of funds into an account.       |
| WITHDRAWAL         | Withdrawal         | Withdrawal of funds from an account.    |
| ACCOUNT_BLOCKING   | Account Blocking   | Blocking of a bank account.             |
| ACCOUNT_UNBLOCKING | Account Unblocking | Removal of a block from a bank account. |
| ACCOUNT_CLOSING    | Account Closing    | Permanent closure of a bank account.    |

### Transfer Operations

| Code                | Name                | Description                                              |
| ------------------- | ------------------- | -------------------------------------------------------- |
| TRANSFER_CREATION   | Transfer Creation   | Creation of a transfer request.                          |
| TRANSFER_APPROVAL   | Transfer Approval   | Approval of a transfer requiring authorization.          |
| TRANSFER_REJECTION  | Transfer Rejection  | Rejection of a transfer request.                         |
| TRANSFER_EXECUTION  | Transfer Execution  | Successful execution of a transfer.                      |
| TRANSFER_EXPIRATION | Transfer Expiration | Expiration of the transfer approval or execution window. |

### Loan Operations

| Code              | Name              | Description                                                 |
| ----------------- | ----------------- | ----------------------------------------------------------- |
| LOAN_APPLICATION  | Loan Application  | Submission of a loan request.                               |
| LOAN_APPROVAL     | Loan Approval     | Approval of a loan request.                                 |
| LOAN_REJECTION    | Loan Rejection    | Rejection of a loan request.                                |
| LOAN_DISBURSEMENT | Loan Disbursement | Transfer of approved loan funds to the destination account. |
| LOAN_PAYMENT      | Loan Payment      | Registration of a payment made against a loan.              |
| LOAN_OVERDUE      | Loan Overdue      | Loan marked as overdue due to unmet obligations.            |
| LOAN_CANCELLATION | Loan Cancellation | Cancellation of a loan in an eligible state.                |

---

# Currency

## Description

Represents a monetary currency supported by the banking institution.

Currency is a business Value Object because its meaning is determined by its controlled values rather than by an independent identity.

## Inherits From

`DomainCatalog`

## Additional Attributes

| Attribute | Type   | Description                       |
| --------- | ------ | --------------------------------- |
| isoCode   | String | ISO 4217 currency code.           |
| symbol    | String | Currency symbol used for display. |

## Allowed Values

| ISO Code | Name                 | Symbol |
| -------- | -------------------- | ------ |
| COP      | Colombian Peso       | $      |
| USD      | United States Dollar | $      |
| EUR      | Euro                 | €      |

---

# Primitive Enumerations

The following concepts are represented as primitive enumerations because they contain fixed technical values and do not require business catalog metadata such as `code`, `name`, or `description`.

---

# ApprovalDecision

## Description

Represents the result of an approval process.

## Values

```text
APPROVED
REJECTED
```

---

# NotificationChannel

## Description

Represents the communication channel used by the system to deliver notifications.

## Values

```text
EMAIL
SMS
PUSH_NOTIFICATION
```

---

# AuditSeverity

## Description

Represents the severity level assigned to an audit event.

## Values

```text
INFORMATION
WARNING
ERROR
CRITICAL
```

---

# Value Object Design Rules

## Immutability

All Value Objects must be immutable after creation.

Their values cannot be modified after the object has been instantiated.

## Equality

Value Objects are compared according to their values rather than object identity.

Two instances containing the same business values represent the same Value Object.

## Controlled Values

Business catalogs must use controlled values defined by the domain.

The application must avoid replacing these concepts with arbitrary strings such as:

```text
"ACTIVE"
"BLOCKED"
"APPROVED"
```

throughout the codebase.

Instead, the corresponding Value Object must be used:

```text
CustomerStatus
UserStatus
AccountStatus
LoanStatus
TransferStatus
```

## Business Versus Technical Enumerations

A business concept should be modeled as a `DomainCatalog` Value Object when it requires:

* a business code;
* a display name;
* a business description;
* controlled domain evolution.

A simple enumeration should be used when the concept represents a fixed technical value without additional business metadata.

## Relationship With Entities

Entities reference Value Objects rather than primitive strings whenever the referenced value represents a controlled business concept.

Examples:

```text
Person.role : SystemRole

Customer.status : CustomerStatus

User.status : UserStatus

BankAccount.accountStatus : AccountStatus

Loan.loanStatus : LoanStatus

Transfer.transferStatus : TransferStatus

Operation.operationType : OperationType

BankAccount.currency : Currency
```

This approach improves type safety, domain expressiveness, maintainability, and consistency with Domain-Driven Design principles.
