# Domain Model

## Introduction

The Domain Model represents the core business entities of the Banking Information Management System. These entities encapsulate the business rules, data, and relationships described in the project specification.

The model follows Object-Oriented Design principles and applies inheritance to eliminate duplicated information while promoting reusability and maintainability.

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

# Entities

---

# Person (Abstract)

## Description

Represents any identifiable entity within the banking system. This abstract class centralizes all common identification and contact information shared by customers and system users.

This class cannot be instantiated directly.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| identification | String | Unique identifier of the entity. Represents a national identification number for natural persons or a tax identification number for businesses. |
| name | String | Full name of a natural person or the legal name of a business. |
| email | String | Primary registered email address. |
| phoneNumber | String | Primary contact phone number. |
| address | String | Registered residential or business address. |

---

# Customer (Abstract)

## Description

Represents any banking customer.

A customer owns one or more banking products and may perform financial operations through the system.

This class specializes the Person entity.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| status | UserStatus | Current operational status of the customer within the banking institution. |

---

# NaturalCustomer

## Description

Represents an individual customer of the bank.

Natural customers may own accounts, request loans, and perform transfers.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| birthDate | LocalDate | Customer's date of birth. The customer must be at least 18 years old. |

---

# BusinessCustomer

## Description

Represents a legal business entity registered as a customer of the bank.

Business customers may own corporate banking products and authorize operational users.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| legalRepresentative | NaturalCustomer | Natural person legally authorized to represent the company before the bank. |

---

# User

## Description

Represents a system account used for authentication and authorization.

Users execute operations within the application and may represent customers or internal employees.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| userId | Integer | Internal unique identifier of the system user. |
| username | String | Login name used during authentication. |
| password | String | Encrypted password stored by the system. |
| role | SystemRole | Business role assigned to the user. |
| status | UserStatus | Current status of the user account. |
| relatedEntityId | String | Identifier of the associated customer whenever applicable. |

---

# BankingProduct (Abstract)

## Description

Represents any financial product managed by the banking system.

All banking products share a common identity and may participate in financial operations.

This class cannot be instantiated directly.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| identifier | String | Unique identifier of the banking product. |

---

# BankAccount

## Description

Represents a bank account owned by a customer.

Accounts store balances and participate in deposits, withdrawals, transfers, and loan disbursements.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| accountType | AccountType | Type of bank account. |
| owner | Customer | Customer who owns the account. |
| currentBalance | BigDecimal | Available account balance. |
| currency | Currency | Currency in which the account operates. |
| accountStatus | AccountStatus | Current operational status of the account. |
| openingDate | LocalDate | Date when the account was created. |

---

# Loan

## Description

Represents a credit product requested by a customer.

Loans follow a lifecycle composed of review, approval, rejection, and disbursement.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| applicant | Customer | Customer requesting the loan. |
| loanType | LoanType | Classification of the loan product. |
| requestedAmount | BigDecimal | Amount requested by the customer. |
| approvedAmount | BigDecimal | Amount approved by the bank. |
| interestRate | BigDecimal | Annual interest rate. |
| termInMonths | Integer | Loan duration expressed in months. |
| loanStatus | LoanStatus | Current state of the loan. |
| approvalDate | LocalDate | Date on which the loan was approved. |
| disbursementDate | LocalDate | Date on which the funds were disbursed. |
| destinationAccount | BankAccount | Account receiving the approved funds. |

---

# Transfer

## Description

Represents the movement of funds between two bank accounts.

Depending on business rules, transfers may require managerial approval before execution.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| sourceAccount | BankAccount | Account from which the funds are debited. |
| destinationAccount | BankAccount | Account receiving the transferred funds. |
| amount | BigDecimal | Amount to be transferred. |
| creationDate | LocalDateTime | Date and time when the transfer was created. |
| approvalDate | LocalDateTime | Date and time when the transfer was approved. |
| transferStatus | TransferStatus | Current execution status of the transfer. |
| createdBy | User | User who created the transfer request. |
| approvedBy | User | User who approved the transfer when required. |

---

# Operation

## Description

Represents any business action executed over a banking product.

Operations provide traceability between users, products, and audit records.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| operationId | Integer | Unique operation identifier. |
| operationType | OperationType | Category of the executed operation. |
| executionDate | LocalDateTime | Date and time when the operation occurred. |
| performedBy | User | User responsible for executing the operation. |
| affectedProduct | BankingProduct | Banking product affected by the operation. |

---

# AuditLog

## Description

Represents the immutable audit trail of the banking system.

Each audit record stores historical information about significant business events and is intended to be persisted in a NoSQL database.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| auditId | String | Unique identifier of the audit record. |
| operationType | OperationType | Type of business operation recorded. |
| operationDate | LocalDateTime | Timestamp when the event occurred. |
| performedBy | User | User responsible for the operation. |
| userRole | SystemRole | User role at the time of execution. |
| affectedProduct | BankingProduct | Banking product involved in the operation. |
| details | Map<String, Object> | Flexible document containing operation-specific information. |