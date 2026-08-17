# Loan Services

## Introduction

This document defines the services belonging to the **Loan** subdomain of the Banking Information Management System.

The services in this subdomain are responsible for managing the lifecycle of loans requested by customers, from the initial application through evaluation, approval or rejection, disbursement, and closure.

Loans are represented by the `Loan` Domain Model and inherit from `BankingProduct`.

```text
BankingProduct
      │
      └── Loan
````

A loan is a banking product associated with a `Customer`. Its lifecycle generates business operations that must be traceable through the `Operation` and `AuditLog` Domain Models.

The services described in this document define the conceptual behavior of the Loan subdomain. More detailed implementation concerns, persistence mappings, REST contracts, and infrastructure-specific behavior must be documented separately.

---

# Domain Model Context

A `Loan` represents a credit product requested by a customer.

Conceptually:

```text
BankingProduct
      │
      └── Loan
             │
             ├── applicant : Customer
             ├── loanType : LoanType
             ├── requestedAmount : BigDecimal
             ├── approvedAmount : BigDecimal
             ├── interestRate : BigDecimal
             ├── termInMonths : Integer
             ├── loanStatus : LoanStatus
             ├── approvalDate : LocalDate
             ├── disbursementDate : LocalDate
             └── destinationAccount : BankAccount
```

The applicant must be represented using the `Customer` Domain Model:

```text
Loan.applicant : Customer
```

The destination account must be represented using the `BankAccount` Domain Model:

```text
Loan.destinationAccount : BankAccount
```

The Domain Model must not replace these relationships with primitive identifiers such as:

```text
String customerId
String accountId
```

Persistence adapters are responsible for translating Domain relationships into their database representation.

---

# Service Design Principles

## Domain Model Parameters

All Loan services and Input Ports must receive Domain Models or Value Objects.

They must never receive:

* Primitive identifiers.
* `String` identifiers.
* Individual attributes as substitutes for Domain Models.
* REST Request DTOs.
* Persistence entities.

### Incorrect

```java
requestLoan(
    String customerId,
    LoanType loanType,
    BigDecimal amount
);
```

### Correct

```java
requestLoan(Loan loan);
```

The Loan Domain Model must contain the information necessary for the operation.

---

# External Information

Loan business rules must first use information available in the Domain Model.

If a rule requires information that is not available in the supplied Domain Model, the service must obtain it through an Output Port.

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
Loan Service
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

# Loan Lifecycle

The Loan lifecycle is represented through `LoanStatus`.

```text
UNDER_REVIEW
      │
      ├──────────────► REJECTED
      │
      ├──────────────► CANCELLED
      │
      ▼
  APPROVED
      │
      ├──────────────► CANCELLED
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

The Domain is responsible for validating whether a status transition is valid.

The persistence layer must never determine whether a transition is permitted.

---

# 1. Request Loan

## Description

Creates a loan application for a `Customer`.

The loan enters the `UNDER_REVIEW` state and becomes subject to the corresponding evaluation process.

---

## Input

```text
Loan
```

The `Loan` Domain Model must contain the information required for the application, including:

* Applicant.
* Loan type.
* Requested amount.
* Term.
* Destination account when required by the business rules.
* Initial loan status.

The service must not receive these attributes as unrelated parameters.

---

## Domain Validations

The service validates information available in the `Loan` Domain Model.

Examples include:

* Applicant is valid.
* Loan type is valid.
* Requested amount is valid.
* Term is valid.
* Destination account is valid when required.
* Initial status is valid.
* The loan can enter `UNDER_REVIEW`.

---

## Customer Validation

The applicant is represented as:

```text
Loan.applicant : Customer
```

If the Domain Model already contains the required customer information, the service validates it directly.

If additional persisted information is required, the service uses:

```text
CustomerRepository
```

through an Output Port.

---

## Account Validation

If the loan requires a destination account, it is represented as:

```text
Loan.destinationAccount : BankAccount
```

If information external to the provided `BankAccount` is required, the service uses:

```text
BankAccountRepository
```

through an Output Port.

---

## Persistence

After successful validation:

```text
LoanRepository
```

is used to persist the loan.

---

## Operation and Audit

Loan application is a business operation.

The operation type is:

```text
LOAN_APPLICATION
```

Conceptually:

```text
Loan
 │
 ▼
Request Loan
 │
 ├── Persist Loan
 │
 └── Operation
        │
        ▼
     AuditLog
```

---

# 2. Consult Loan

## Description

Retrieves a loan represented by the `Loan` Domain Model.

Persistence entities must never be exposed outside the persistence adapter.

---

## Input

```text
Loan
```

The service must not define an application-level contract such as:

```java
consultLoan(String loanId);
```

The required identification/context must be represented by the Domain Model.

Conceptually:

```java
consultLoan(Loan loan);
```

---

## Processing

```text
Loan
 │
 ▼
LoanRepository
 │
 ▼
Loan
```

---

# 3. Evaluate Loan

## Description

Evaluates a loan application that is currently under review.

The evaluation determines whether the application satisfies the business conditions required for approval.

This service coordinates the evaluation but does not delegate business rules to the database.

---

## Input

```text
Loan
```

The service must receive the Loan Domain Model.

---

## Domain Validations

The service validates information contained in the loan, such as:

* Loan type.
* Requested amount.
* Term.
* Applicant.
* Destination account when applicable.
* Current loan status.

If additional customer or account information is required, the corresponding Output Port must be used.

---

## External Information

Examples of external information that may require Output Ports include:

```text
CustomerRepository
BankAccountRepository
```

The service may also use other Output Ports if the business rules require information not represented in the Domain Model.

---

## Result

The evaluation may lead to:

```text
UNDER_REVIEW
      │
      ├── APPROVED
      │
      └── REJECTED
```

The actual transition must be determined by the Domain rules.

---

# 4. Approve Loan

## Description

Approves a loan application.

The loan must satisfy the conditions defined by the business rules before entering the `APPROVED` state.

This operation is restricted to users with the `INTERNAL_ANALYST` role.

---

## Input

```text
User
Loan
```

The service must not receive:

```java
approveLoan(String loanId);
```

or:

```java
approveLoan(String loanId, BigDecimal amount);
```

The correct signature receives Domain Models:

```java
approveLoan(User user, Loan loan);
```

---

## Authorization

Before applying any domain validation, the service must verify that the requesting user is authorized.

The rule is:

```text
User.role == INTERNAL_ANALYST
```

If the user does not have the `INTERNAL_ANALYST` role, the service must raise a business exception without proceeding.

The authorization rule must not be delegated to a controller or adapter.

---

## Domain Validations

The service validates:

* Current loan status.
* Applicant eligibility.
* Requested amount.
* Approved amount.
* Loan type.
* Term.
* Interest rate.
* Destination account when required.
* Validity of the `UNDER_REVIEW → APPROVED` transition.

The exact approval rules belong to the Domain.

---

## Approved Amount

The `approvedAmount` belongs to the `Loan` Domain Model.

The service must not use an independent `BigDecimal` parameter as a substitute for the Domain Model.

If the approval process determines a different amount from the requested amount, the Loan Domain Model must be updated accordingly.

---

## Approval Date

The approval date belongs to:

```text
Loan.approvalDate
```

It must be set as part of the Domain operation when the loan transitions to `APPROVED`.

---

## Persistence

The updated loan is persisted through:

```text
LoanRepository
```

---

## Operation and Audit

Approval generates:

```text
LOAN_APPROVAL
```

Conceptually:

```text
Loan
 │
 ▼
Approve Loan
 │
 ├── Update Loan
 │
 ├── LoanRepository
 │
 ├── OperationRepository
 │
 └── AuditRepository
```

---

# 5. Reject Loan

## Description

Rejects a loan application.

The loan transitions from an evaluable state to:

```text
REJECTED
```

This operation is restricted to users with the `INTERNAL_ANALYST` role.

---

## Input

```text
User
Loan
```

The correct signature receives Domain Models:

```java
rejectLoan(User user, Loan loan);
```

The service must receive both Domain Models.

---

## Domain Validations

The service validates:

* Current loan status.
* Validity of the rejection transition.
* Conditions that permit rejection.
* Required applicant information when applicable.

If rejection requires information external to the Loan Domain Model, the service must obtain it through an Output Port.

---

## Persistence

The updated loan is persisted through:

```text
LoanRepository
```

---

## Operation and Audit

The operation type is:

```text
LOAN_REJECTION
```

The event must be registered in the audit trail according to the business audit rules.

---

# 6. Disburse Loan

## Description

Disburses the approved loan amount to the destination bank account.

The disbursement represents the movement of funds associated with the loan.

---

## Input

```text
Loan
```

The destination account is obtained from:

```text
Loan.destinationAccount
```

and not from a primitive account identifier.

---

## Domain Validations

The service validates:

* Loan is approved.
* Approved amount exists.
* Destination account is valid.
* Destination account allows the required operation.
* Currency compatibility when required.
* The loan has not already been disbursed.
* The `APPROVED → DISBURSED` transition is valid.

---

## Bank Account Interaction

The destination account is a `BankAccount` Domain Model.

The service must use the appropriate Bank Account behavior to receive the funds.

The service must not directly modify a database balance.

Conceptually:

```text
Loan
 │
 ▼
Disburse Loan
 │
 ├── Validate Loan
 │
 ├── Validate BankAccount
 │
 ├── Credit destination account
 │
 ├── Update Loan
 │
 ├── LoanRepository
 │
 ├── BankAccountRepository
 │
 ├── OperationRepository
 │
 └── AuditRepository
```

---

## Disbursement Date

The disbursement date belongs to:

```text
Loan.disbursementDate
```

It is established when the loan is successfully disbursed.

---

## Operation and Audit

The operation type is:

```text
LOAN_DISBURSEMENT
```

The disbursement must be traceable through the operation and audit mechanisms.

---

# 7. Cancel Loan

## Description

Cancels a loan that is in a state that permits cancellation.

The loan transitions to:

```text
CANCELLED
```

Valid source states for cancellation are:

```text
UNDER_REVIEW → CANCELLED
APPROVED     → CANCELLED
OVERDUE      → CANCELLED
```

---

## Input

```text
User
Loan
```

---

## Authorization

The requesting user must be authorized to cancel a loan.

The exact authorization rule depends on the current loan state and the user's role.

---

## Domain Validations

The service validates:

* Current loan status allows cancellation.
* Validity of the transition to `CANCELLED`.
* Outstanding obligations when applicable.
* Any other conditions defined by the Domain.

If determining whether the loan has outstanding obligations requires external information, the service must obtain it through an Output Port.

---

## Persistence

The updated loan is persisted through:

```text
LoanRepository
```

---

## Operation and Audit

Cancelling a loan is a significant business event and must be registered according to the audit rules.

The operation type is:

```text
LOAN_CANCELLATION
```

---

# 8. Consult Loan Status

## Description

Provides the current status of a loan.

The status belongs directly to:

```text
Loan.loanStatus
```

Therefore, when the current `Loan` Domain Model is already available, no external lookup is required.

If the current persisted state is required, the service retrieves the Domain Model through:

```text
LoanRepository
```

---

# 9. Consult Loan Details

## Description

Provides the complete business representation of a loan.

The result must be a `Loan` Domain Model.

The service must not expose:

* Database entities.
* Persistence documents.
* SQL records.
* Internal repository structures.

---

# 10. Validate Loan Eligibility

## Description

Determines whether a customer and a loan application satisfy the conditions required to proceed with the loan process.

---

## Input

The operation must receive the appropriate Domain Model representing the loan:

```text
Loan
```

The applicant is available through:

```text
Loan.applicant
```

---

## Validation

The service evaluates rules based on:

* Applicant information.
* Loan type.
* Requested amount.
* Term.
* Destination account.
* Current loan status.

If eligibility depends on information not available in the Domain Model, the service calls the appropriate Output Port.

---

# Output Ports

The Loan subdomain may require the following Output Ports:

```text
LoanRepository
CustomerRepository
BankAccountRepository
OperationRepository
AuditRepository
```

Additional Output Ports may be introduced when future business requirements require external information.

---

# LoanRepository

## Description

Defines persistence operations required by the Loan subdomain.

Conceptually:

```java
interface LoanRepository {

    Loan save(Loan loan);

    Loan find(Loan loan);

    boolean exists(Loan loan);
}
```

The exact methods should be refined according to the required use cases.

The interface belongs to:

```text
domain/ports/out/
```

The MySQL adapter implements this port.

---

# CustomerRepository

## Description

Provides persisted Customer information when the Loan service requires information that cannot be determined from the `Customer` Domain Model already contained in the Loan.

Conceptually:

```java
interface CustomerRepository {

    Customer find(Customer customer);

    boolean exists(Customer customer);
}
```

The Customer repository contract should be defined in the Customer subdomain documentation.

---

# BankAccountRepository

## Description

Provides persisted `BankAccount` information when loan operations require information external to the Bank Account Domain Model.

It is particularly relevant during:

* Loan disbursement.
* Destination account validation.

The Loan service communicates with this repository through an Output Port.

---

# OperationRepository

## Description

Persists business operations generated by the Loan lifecycle.

Loan operations include, among others:

```text
LOAN_APPLICATION
LOAN_APPROVAL
LOAN_REJECTION
LOAN_DISBURSEMENT
```

The service never accesses the operation database directly.

---

# AuditRepository

## Description

Persists the audit records generated by Loan business operations.

Audit persistence is performed through the Output Port:

```text
AuditRepository
```

The adapter is responsible for the MongoDB implementation.

---

# Input Ports

The Loan subdomain exposes the following conceptual use cases:

```text
RequestLoanUseCase
ConsultLoanUseCase
EvaluateLoanUseCase
ApproveLoanUseCase
RejectLoanUseCase
DisburseLoanUseCase
CloseLoanUseCase
ConsultLoanStatusUseCase
ConsultLoanDetailsUseCase
ValidateLoanEligibilityUseCase
```

Each Input Port must follow the Domain Model parameter rule.

---

# Example Input Ports

```java
interface RequestLoanUseCase {

    Loan request(Loan loan);
}
```

```java
interface ApproveLoanUseCase {

    Loan approve(User user, Loan loan);
}
```

```java
interface RejectLoanUseCase {

    Loan reject(User user, Loan loan);
}
```

```java
interface DisburseLoanUseCase {

    Loan disburse(Loan loan);
}
```

---

# Loan Application Flow

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
Loan Domain Model
     │
     ▼
RequestLoanUseCase
     │
     ▼
Loan Service
     │
     ├── Validate Domain Rules
     │
     ├── CustomerRepository
     │
     ├── LoanRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```

---

# Loan Approval Flow

```text
Loan
 │
 ▼
ApproveLoanUseCase
 │
 ▼
Loan Service
 │
 ├── Validate Loan
 │
 ├── Validate Applicant
 │
 ├── Validate Approval Rules
 │
 ├── Update Loan
 │
 ├── LoanRepository
 │
 ├── OperationRepository
 │
 └── AuditRepository
```

---

# Loan Rejection Flow

```text
Loan
 │
 ▼
RejectLoanUseCase
 │
 ▼
Loan Service
 │
 ├── Validate Status
 │
 ├── Apply Domain Transition
 │
 ├── LoanRepository
 │
 ├── OperationRepository
 │
 └── AuditRepository
```

---

# Loan Disbursement Flow

```text
Loan
 │
 ▼
DisburseLoanUseCase
 │
 ▼
Loan Service
 │
 ├── Validate Loan
 │
 ├── Validate Destination BankAccount
 │
 ├── Credit BankAccount
 │
 ├── Update Loan
 │
 ├── LoanRepository
 │
 ├── BankAccountRepository
 │
 ├── OperationRepository
 │
 └── AuditRepository
```

---

# Operation and Audit

`Loan` is a `BankingProduct`.

Therefore, significant lifecycle events involving a loan generate business operations.

Examples:

```text
LOAN_APPLICATION
LOAN_APPROVAL
LOAN_REJECTION
LOAN_DISBURSEMENT
```

Conceptually:

```text
Loan
 │
 ▼
Business Action
 │
 ├──────────────► Loan updated
 │
 ▼
Operation
 │
 ▼
AuditLog
```

The Loan service does not directly persist the `AuditLog`.

It uses:

```text
AuditRepository
```

through the Domain Output Port.

---

# Domain Validation Strategy

## Information Available in Loan

The following attributes belong directly to the `Loan` Domain Model:

```text
applicant
loanType
requestedAmount
approvedAmount
interestRate
termInMonths
loanStatus
approvalDate
disbursementDate
destinationAccount
```

Business rules involving these attributes should be evaluated using the Domain Model.

---

## Information Available Through Relationships

The Loan Domain Model contains relationships with:

```text
Customer
BankAccount
```

Therefore, rules involving the customer or destination account should use these Domain Models whenever their required information is already available.

---

## External Information

If a rule requires information not contained in the supplied Domain Models, the service must use an Output Port.

For example:

```text
Loan Service
     │
     ├── CustomerRepository
     │
     ├── BankAccountRepository
     │
     ├── LoanRepository
     │
     ├── OperationRepository
     │
     └── AuditRepository
```

The service must never implement database queries itself.

---

# Loan Status

The `Loan` uses the `LoanStatus` Value Object.

The supported lifecycle states are:

```text
UNDER_REVIEW
APPROVED
REJECTED
DISBURSED
CLOSED
```

The Domain is responsible for enforcing valid state transitions.

Conceptually:

```text
UNDER_REVIEW
     │
     ├──────────► REJECTED
     │
     ▼
 APPROVED
     │
     ▼
 DISBURSED
     │
     ▼
  CLOSED
```

---

# Loan Exceptions

Conceptual exceptions for this subdomain include:

```text
LoanNotFoundException
InvalidLoanException
InvalidLoanStatusException
InvalidLoanStatusTransitionException
LoanAlreadyApprovedException
LoanAlreadyRejectedException
LoanAlreadyDisbursedException
LoanAlreadyClosedException
CustomerNotEligibleException
InvalidLoanAmountException
InvalidLoanTermException
InvalidApprovedAmountException
InvalidInterestRateException
InvalidDestinationAccountException
LoanDisbursementException
```

The complete exception catalog should be defined separately in the Domain Exceptions documentation.

---

# Architectural Constraints

The following rules are mandatory for the Loan subdomain:

1. `Loan` is a Domain Model.
2. `Loan` inherits from `BankingProduct`.
3. `Loan.applicant` is represented by `Customer`.
4. `Loan.destinationAccount` is represented by `BankAccount`.
5. Customer and account relationships must not be represented by primitive identifiers in the Domain Model.
6. Loan services must receive Domain Models or Value Objects.
7. Loan services must never receive primitive identifiers as application-level parameters.
8. Loan services must never receive isolated attributes when those attributes belong to a Domain Model.
9. REST DTOs must never enter the Domain layer.
10. Persistence entities must never enter the Domain layer.
11. Business validations belong to the Domain.
12. Information already available in the Domain Model must be validated without unnecessary external calls.
13. External information must be obtained through Output Ports.
14. Loan services must always communicate with external resources through Output Ports.
15. Loan services must never access MySQL directly.
16. Loan services must never access MongoDB directly.
17. Loan services must never access SQL, JPA, or persistence repositories directly.
18. `LoanRepository` is responsible for Loan persistence.
19. `CustomerRepository` provides external Customer information when required.
20. `BankAccountRepository` provides external Bank Account information when required.
21. `OperationRepository` persists business operations.
22. `AuditRepository` persists audit information.
23. Loan status transitions must be validated by Domain rules.
24. A loan must not be approved if the Domain rules for approval are not satisfied.
25. A rejected loan must not be disbursed.
26. A loan must not be disbursed before it is approved.
27. A loan must not be disbursed more than once.
28. Loan disbursement must operate on the `BankAccount` Domain Model rather than directly modifying database balances.
29. Significant Loan lifecycle events must generate an `Operation`.
30. Relevant Loan operations must be recorded in the `AuditLog`.
31. Persistence entities must remain inside the persistence adapter.
32. The Domain must remain independent of infrastructure technologies.
33. The Loan subdomain must be fully testable without requiring MySQL, MongoDB, REST, or infrastructure components.

```
```
