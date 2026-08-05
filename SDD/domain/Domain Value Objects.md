# Domain Value Objects

## Introduction

Value Objects represent immutable concepts within the banking domain. Unlike Entities, they do not have their own identity; instead, they are defined entirely by their attributes.

These objects encapsulate controlled business values, improve domain expressiveness, and prevent the use of primitive types or scattered string literals throughout the application.

---

# Value Object Hierarchy

```text
DomainCatalog (Abstract)
├── SystemRole
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

All controlled business values inherit from this class, ensuring a consistent structure across the application.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| code | String | Unique business identifier. |
| name | String | Human-readable name displayed within the application. |
| description | String | Business definition of the catalog value. |

---

# SystemRole

## Description

Represents the responsibilities and permissions assigned to a system user.

Roles determine which business operations a user may perform.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| NATURAL_CUSTOMER | Natural Customer | Individual banking customer. |
| BUSINESS_CUSTOMER | Business Customer | Corporate banking customer. |
| TELLER_EMPLOYEE | Teller Employee | Performs branch operations. |
| COMMERCIAL_EMPLOYEE | Commercial Employee | Manages customer relationships and loan requests. |
| BUSINESS_OPERATOR | Business Operator | Performs operations on behalf of business customers. |
| BUSINESS_SUPERVISOR | Business Supervisor | Approves business transfers requiring authorization. |
| INTERNAL_ANALYST | Internal Analyst | Reviews and approves loan applications. |

---

# UserStatus

## Description

Represents whether a customer or system user can interact with the banking platform.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| ACTIVE | Active | User can access the system normally. |
| INACTIVE | Inactive | User exists but cannot perform operations. |
| BLOCKED | Blocked | User access has been suspended. |

---

# AccountStatus

## Description

Represents the operational state of a bank account.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| ACTIVE | Active | Account is fully operational. |
| BLOCKED | Blocked | Transactions are temporarily disabled. |
| CLOSED | Closed | Account has been permanently closed. |

---

# LoanStatus

## Description

Represents the lifecycle of a loan.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| UNDER_REVIEW | Under Review | Loan request is under evaluation. |
| APPROVED | Approved | Loan has been approved. |
| REJECTED | Rejected | Loan request was rejected. |
| DISBURSED | Disbursed | Approved funds have been transferred. |
| CLOSED | Closed | Loan has been fully settled. |

---

# TransferStatus

## Description

Represents the execution state of a transfer.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| PENDING | Pending | Transfer has been created. |
| WAITING_FOR_APPROVAL | Waiting for Approval | Transfer requires managerial approval. |
| APPROVED | Approved | Transfer has been approved and is ready for execution. |
| EXECUTED | Executed | Funds have been successfully transferred. |
| REJECTED | Rejected | Transfer request has been denied. |
| EXPIRED | Expired | Approval time window has expired. |

---

# AccountType

## Description

Represents the different bank account products offered by the institution.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| SAVINGS | Savings Account | Standard interest-bearing deposit account. |
| CHECKING | Checking Account | Transaction account intended for frequent operations. |
| BUSINESS | Business Account | Account designed for corporate customers. |

---

# LoanType

## Description

Represents the different credit products provided by the bank.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| PERSONAL | Personal Loan | Loan intended for personal use. |
| MORTGAGE | Mortgage Loan | Loan secured by real estate. |
| VEHICLE | Vehicle Loan | Loan used to finance vehicle purchases. |
| BUSINESS | Business Loan | Loan intended for business financing. |

---

# OperationType

## Description

Represents the type of business operation executed within the banking system.

Every audit record and business transaction must reference one operation type.

## Inherits From

DomainCatalog

## Allowed Values

| Code | Name | Description |
|------|------|-------------|
| ACCOUNT_OPENING | Account Opening | Creation of a new bank account. |
| DEPOSIT | Deposit | Deposit of funds into an account. |
| WITHDRAWAL | Withdrawal | Withdrawal of funds from an account. |
| TRANSFER | Transfer | Movement of funds between accounts. |
| LOAN_APPLICATION | Loan Application | Submission of a loan request. |
| LOAN_APPROVAL | Loan Approval | Approval of a loan request. |
| LOAN_REJECTION | Loan Rejection | Rejection of a loan request. |
| LOAN_DISBURSEMENT | Loan Disbursement | Transfer of approved loan funds. |
| TRANSFER_APPROVAL | Transfer Approval | Approval of a transfer requiring authorization. |
| TRANSFER_REJECTION | Transfer Rejection | Rejection of a transfer request. |

---

# Currency

## Description

Represents the monetary currency supported by banking products.

## Inherits From

DomainCatalog

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| isoCode | String | ISO 4217 currency code. |
| symbol | String | Currency symbol. |

## Allowed Values

| ISO Code | Name | Symbol |
|-----------|------|--------|
| COP | Colombian Peso | $ |
| USD | United States Dollar | $ |
| EUR | Euro | € |

---

# Primitive Enumerations

The following concepts are simple enumerations because they represent fixed technical values without business behavior.

---

## ApprovalDecision

### Description

Represents the result of an approval process.

### Values

- APPROVED
- REJECTED

---

## NotificationChannel

### Description

Represents the communication channel used by the system.

### Values

- EMAIL
- SMS
- PUSH_NOTIFICATION

---

## AuditSeverity

### Description

Represents the severity level of an audit event.

### Values

- INFORMATION
- WARNING
- ERROR
- CRITICAL

---

# Design Notes

- All business catalogs inherit from **DomainCatalog**.
- Value Objects are immutable.
- Equality is determined by their values rather than object identity.
- Business entities reference Value Objects instead of primitive strings.
- Primitive Enumerations are reserved exclusively for technical concepts that do not encapsulate business rules or behavior.
- This approach improves maintainability, consistency, and alignment with Domain-Driven Design (DDD) principles while supporting future domain evolution.