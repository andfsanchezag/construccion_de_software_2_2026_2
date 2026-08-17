# Authorization Services

## Introduction

This document defines the services responsible for **authorization** within the Banking Information Management System.

Authorization determines whether an authenticated `User` is allowed to perform a specific business operation according to the user's `SystemRole` and the business rules associated with that operation.

Authentication and authorization are separate responsibilities:

```text
Authentication
     │
     ▼
Who is the User?
     │
     ▼
Authorization
     │
     ▼
What can the User do?
````

Authentication is responsible for validating credentials and establishing the identity of the user.

Authorization is responsible for determining whether that authenticated user has permission to execute a requested operation.

The authorization services do not implement the business operation itself. They determine whether the operation may be initiated by the current `User`.

---

# Domain Model Context

Authorization is based primarily on the following Domain Models:

```text
User
SystemRole
BankingProduct
Operation
```

The `User` Domain Model contains:

```text
User
├── userId
├── username
├── password
├── role : SystemRole
├── status : UserStatus
└── customer : Customer
```

The user's authorization role is represented by:

```text
User.role : SystemRole
```

The service must therefore work with the `User` Domain Model rather than receiving a primitive role or user identifier.

---

# Authorization Principles

## User as Domain Model

Authorization services must receive a `User` Domain Model.

### Incorrect

```java
authorize(
    String username,
    String role
);
```

### Correct

```java
authorize(
    User user,
    ...
);
```

The role must be obtained from:

```text
User.role
```

and not passed independently as a `String`.

---

# Operation as Domain Model

When authorization is required for a business operation, the authorization service must receive the corresponding Domain Model representing the operation or business context.

For example:

```text
BankAccount
Loan
Transfer
```

These models inherit from:

```text
BankingProduct
```

Therefore authorization may be evaluated using:

```text
User
BankingProduct
```

or a more specific Domain Model when the authorization rule requires it.

---

# No Primitive Identifiers

Authorization services must not use primitive identifiers as substitutes for Domain relationships.

### Incorrect

```java
authorizeTransfer(
    String userId,
    String transferId
);
```

### Correct

```java
authorizeTransfer(
    User user,
    Transfer transfer
);
```

The same principle applies to:

* Customer.
* BankAccount.
* Loan.
* Transfer.
* Operation.
* User.

---

# Authorization Scope

Authorization is divided into two distinct scopes that must not be conflated.

## Read Authorization

Determines whether a user is allowed to **consult or read** information belonging to a domain object.

Examples:

```text
canConsultCustomer(User user, Customer customer)
canConsultLoan(User user, Loan loan)
canConsultBankAccount(User user, BankAccount account)
canConsultTransfer(User user, Transfer transfer)
canConsultAuditLog(User user)
```

Read authorization typically depends on the user's role and the ownership relationship between the user's associated customer and the domain object.

## Execute Authorization

Determines whether a user is allowed to **execute or modify** a domain operation.

Examples:

```text
canExecuteDeposit(User user, BankAccount account)
canExecuteWithdrawal(User user, BankAccount account)
canApproveLoan(User user, Loan loan)
canApproveTransfer(User user, Transfer transfer)
canBlockAccount(User user, BankAccount account)
```

Execute authorization typically depends on the user's role, the current state of the affected domain object, and the applicable business rules.

---

# Authorization Responsibilities

Authorization services are responsible for:

* Determining whether a user has the required role.
* Validating that the user is allowed to **consult** a particular domain object (read scope).
* Validating that the user is allowed to **execute** a particular business operation (execute scope).
* Validating authorization according to the affected Domain Model.
* Supporting role-based authorization.
* Supporting authorization rules specific to banking products.
* Supporting approval-related authorization.
* Preventing users from executing operations outside their responsibilities.
* Preventing users from consulting information outside their visibility scope.

Authorization services are not responsible for:

* Authenticating credentials.
* Validating passwords.
* Generating JWT tokens.
* Persisting users.
* Executing banking operations.
* Updating banking products.
* Implementing REST security filters.

---

# System Roles

Authorization uses the `SystemRole` Value Object.

The currently defined roles are:

```text
NATURAL_CUSTOMER
BUSINESS_CUSTOMER
TELLER_EMPLOYEE
COMMERCIAL_EMPLOYEE
BUSINESS_OPERATOR
BUSINESS_SUPERVISOR
INTERNAL_ANALYST
```

The authorization service must use the `SystemRole` Domain Value Object instead of raw strings.

---

# Role Authorization Rules

The following rules define what each role is authorized to do within the system.

## NATURAL_CUSTOMER

* May consult and operate exclusively on their own banking products.
* May not access products belonging to other customers.

## BUSINESS_CUSTOMER

* Treated as the owning identity of business banking products.
* Operations are typically performed through associated `BUSINESS_OPERATOR` or `BUSINESS_SUPERVISOR` users.

## TELLER_EMPLOYEE

* May consult any customer and their products.
* May perform deposit, withdrawal, account opening, and account closure operations.
* May not approve loans or transfers requiring authorization.

## COMMERCIAL_EMPLOYEE

* May consult **any customer** without restriction.
* May consult any banking product associated with any customer.
* May perform loan-related operations within their responsibilities.
* Customer access is unrestricted — no assignment or ownership relationship is required.

## BUSINESS_OPERATOR

* May perform operations on behalf of the `BusinessCustomer` associated with their `User.customer`.
* Access is restricted to products belonging to the associated `BusinessCustomer`.

## BUSINESS_SUPERVISOR

* May approve transfers on behalf of the `BusinessCustomer` associated with their `User.customer`.
* Access for approval is restricted to transfers initiated within their associated business customer's scope.

## INTERNAL_ANALYST

* May consult any customer and any banking product.
* Is the only role authorized to approve or reject loans.
* May register and manage internal employee users.

---

# User Status

Authorization must also consider the `UserStatus` associated with the authenticated `User`.

Supported statuses are:

```text
ACTIVE
INACTIVE
BLOCKED
```

A user that is not operationally active must not be authorized to execute protected business operations.

Conceptually:

```text
User
 │
 ├── status
 │
 └── role
       │
       ▼
Authorization
```

---

# 1. Authorize Operation

## Description

Determines whether a `User` is authorized to perform a specific business operation.

The operation context must be represented by a Domain Model.

---

## Input

Conceptually:

```text
User
Operation
```

The service must not receive:

```java
authorize(
    String userId,
    String operationType
);
```

Instead, the authorization context must be represented by Domain Models.

---

## Processing

```text
User
 │
 ├── status
 └── role
       │
       ▼
Authorization Service
       │
       ├── Validate User Status
       │
       ├── Validate Role
       │
       └── Validate Operation Context
```

---

## Result

The service determines whether authorization is granted.

Conceptually:

```text
AUTHORIZED
UNAUTHORIZED
```

The exact representation of the result should be defined by the Domain model and service contract.

---

# 2. Authorize Product Operation

## Description

Determines whether a `User` may execute an operation over a `BankingProduct`.

Banking products include:

```text
BankAccount
Loan
Transfer
```

---

## Input

```text
User
BankingProduct
Operation
```

The concrete product may be:

```text
BankAccount
Loan
Transfer
```

---

## Processing

```text
User
 │
 ▼
Authorization Service
 │
 ├── Validate User Status
 │
 ├── Validate User Role
 │
 ├── Validate Product
 │
 └── Validate Operation
```

---

# 3. Authorize Bank Account Operation

## Description

Determines whether a `User` is authorized to perform an operation over a `BankAccount`.

The service receives:

```text
User
BankAccount
Operation
```

Authorization may depend on:

* User role.
* User status.
* Account ownership.
* Type of account.
* Type of operation.
* Other Domain rules.

Information already available in the Domain Models must be used directly.

If external information is required, the service must use an Output Port.

---

# 4. Authorize Loan Operation

## Description

Determines whether a `User` may execute an operation over a `Loan`.

The service receives:

```text
User
Loan
Operation
```

Examples of protected loan operations include:

```text
LOAN_APPLICATION
LOAN_APPROVAL
LOAN_REJECTION
LOAN_DISBURSEMENT
```

Authorization for `LOAN_APPROVAL` and `LOAN_REJECTION` requires:

```text
User.role == INTERNAL_ANALYST
```

Authorization may additionally depend on the current state of the loan.

---

# 5. Authorize Transfer Operation

# 5. Authorize Transfer Operation

## Description

Determines whether a `User` may execute an operation over a `Transfer`.

The service receives:

```text
User
Transfer
Operation
```

Authorization may depend on:

* User role.
* User status.
* Transfer state.
* Customer relationship.
* Business customer relationship.
* Transfer approval requirements.

---

# 6. Authorize Transfer Approval

## Description

Determines whether a `User` is authorized to approve a transfer requiring authorization.

The service receives:

```text
User
Transfer
```

The authorization decision must be based on the Domain Models.

For example, the role:

```text
BUSINESS_SUPERVISOR
```

may be relevant to business transfer approval according to the defined business rules.

The service must not determine authorization from a raw string such as:

```text
"BUSINESS_SUPERVISOR"
```

Instead:

```text
User.role : SystemRole
```

must be evaluated.

---

# 7. Authorize Loan Approval

## Description

Determines whether a `User` is authorized to approve a loan.

The service receives:

```text
User
Loan
```

The authorization must consider:

* User status must be `ACTIVE`.
* User role must be `INTERNAL_ANALYST`.
* Current Loan status must allow approval (`UNDER_REVIEW`).

The rule is:

```text
User.role == INTERNAL_ANALYST
    AND
User.status == ACTIVE
    AND
Loan.loanStatus == UNDER_REVIEW
```

The service must not approve the loan itself.

Its responsibility ends with determining whether the user is authorized.

Conceptually:

```text
User + Loan
      │
      ▼
Authorize Loan Approval
      │
      ▼
Authorization Result
      │
      ▼
Loan Service
      │
      └── Execute approval if authorized
```

---

# 8. Authorize Customer Operation

## Description

Determines whether a user may execute an operation on behalf of or over a `Customer`.

The relevant relationship is represented in the Domain Model.

For example:

```text
User.customer : Customer
```

The service must compare Domain Models rather than identifiers.

### Incorrect

```java
user.getCustomer().getIdentification()
    .equals(customer.getIdentification());
```

The authorization model should instead operate using the appropriate Domain relationship:

```text
User
 │
 └── customer : Customer
```

and:

```text
Customer
```

---

# 9. Authorize Business Customer Operation

## Description

Determines whether a user may perform an operation on behalf of a `BusinessCustomer`.

This is particularly relevant to roles such as:

```text
BUSINESS_OPERATOR
BUSINESS_SUPERVISOR
```

The service must evaluate the relationships represented in the Domain Models.

Conceptually:

```text
User
 │
 ├── role
 │
 └── customer : Customer
                     │
                     ▼
              BusinessCustomer
```

If additional persisted information is required, the service must use the appropriate Output Port.

---

# 10. Validate User Authorization Status

## Description

Validates whether a `User` is currently eligible to execute protected operations.

The service evaluates:

```text
User.status
```

The supported statuses are:

```text
ACTIVE
INACTIVE
BLOCKED
```

Only users whose status satisfies the authorization rules may proceed.

No database lookup is required when the required status is already present in the `User` Domain Model.

---

# 11. Validate Role Authorization

## Description

Determines whether the `SystemRole` associated with a `User` permits the requested operation.

The service receives the `User` Domain Model and obtains:

```text
User.role
```

It must not receive the role independently.

---

## Conceptual Processing

```text
User
 │
 ▼
User.role
 │
 ▼
SystemRole
 │
 ▼
Authorization Rules
 │
 ▼
Authorized / Unauthorized
```

---

# 12. Validate Customer Ownership

## Description

Determines whether a customer is authorized to operate over a specific banking product.

For example, a customer may only be authorized to perform operations over products that belong to that customer.

The service receives the relevant Domain Models.

Conceptually:

```text
User
 │
 └── customer : Customer
                     │
                     ▼
                Customer
                     │
                     ▼
                BankAccount
```

The comparison must use Domain relationships and attributes.

If determining ownership requires information not present in the supplied models, the service must use an Output Port.

---

# 13. Validate Business Operator Authorization

## Description

Determines whether a `BUSINESS_OPERATOR` can perform an operation on behalf of a `BusinessCustomer`.

The service receives the appropriate Domain Models:

```text
User
BusinessCustomer
BankingProduct
```

The service validates the relationship using Domain information.

---

# 14. Validate Business Supervisor Authorization

## Description

Determines whether a `BUSINESS_SUPERVISOR` is authorized to perform approval operations for a business customer.

The service receives:

```text
User
Transfer
```

and uses the Domain relationships to determine whether authorization is valid.

---

# 15. Validate Internal Analyst Authorization

## Description

Determines whether an `INTERNAL_ANALYST` is authorized to perform internal banking operations requiring analyst privileges.

This authorization is particularly relevant to operations such as:

```text
LOAN_APPROVAL
LOAN_REJECTION
```

The service receives:

```text
User
Loan
```

and validates the user's role and the current loan state.

---

# Authorization and Business Services

Authorization is performed before executing protected business operations.

For example:

```text
Request
   │
   ▼
Controller
   │
   ▼
Input Port
   │
   ▼
Authorization Service
   │
   ├── Authorized
   │       │
   │       ▼
   │   Business Service
   │
   └── Unauthorized
           │
           ▼
       Business Exception
```

The authorization service does not execute the business operation.

---

# Authorization and Authentication

Authentication is responsible for identifying the user.

Authorization uses the authenticated `User` Domain Model.

Conceptually:

```text
Credentials
    │
    ▼
Authentication Service
    │
    ▼
User
    │
    ▼
Authorization Service
    │
    ▼
Business Service
```

The authorization service must not validate passwords.

Password validation belongs to the authentication services.

---

# JWT Relationship

The authentication process produces a JWT after successful credential validation.

The JWT may contain information required by the technical security mechanism.

However, the Domain authorization services must operate using the `User` Domain Model.

The Domain must not depend directly on:

* JWT libraries.
* HTTP headers.
* Authentication filters.
* Security frameworks.

The conversion from authenticated security information to a `User` Domain Model belongs to the application/security boundary.

Conceptually:

```text
JWT
 │
 ▼
Security Adapter
 │
 ▼
User Domain Model
 │
 ▼
Authorization Service
```

---

# Output Ports

Authorization services must use Output Ports whenever external information is required.

Possible Output Ports include:

```text
UserRepository
CustomerRepository
BankAccountRepository
LoanRepository
TransferRepository
```

The required repository depends on the authorization rule being evaluated.

For example:

```text
Authorization of account operation
        │
        ▼
BankAccountRepository
```

or:

```text
Authorization of business customer relationship
        │
        ▼
CustomerRepository
```

---

# UserRepository

## Description

Provides User information when the supplied `User` Domain Model does not contain sufficient information to evaluate authorization.

Conceptually:

```java
interface UserRepository {

    User find(User user);

    boolean exists(User user);
}
```

The exact contract must be defined according to the required use cases.

---

# CustomerRepository

## Description

Provides Customer information when authorization requires information not available in the supplied Customer Domain Model.

The repository is accessed only through an Output Port.

---

# BankAccountRepository

## Description

Provides Bank Account information when authorization requires external information about an account.

The authorization service must never access account persistence directly.

---

# LoanRepository

## Description

Provides Loan information when authorization requires external information about a loan.

For example, authorization may depend on the persisted state of the loan.

---

# TransferRepository

## Description

Provides Transfer information when authorization requires external information about a transfer.

For example, transfer approval authorization may require the current persisted transfer state.

---

# Input Ports

The Authorization subdomain exposes the following conceptual use cases:

```text
AuthorizeOperationUseCase
AuthorizeProductOperationUseCase
AuthorizeBankAccountOperationUseCase
AuthorizeLoanOperationUseCase
AuthorizeTransferOperationUseCase
AuthorizeTransferApprovalUseCase
AuthorizeLoanApprovalUseCase
AuthorizeCustomerOperationUseCase
AuthorizeBusinessCustomerOperationUseCase
ValidateUserAuthorizationStatusUseCase
ValidateRoleAuthorizationUseCase
ValidateCustomerOwnershipUseCase
ValidateBusinessOperatorAuthorizationUseCase
ValidateBusinessSupervisorAuthorizationUseCase
ValidateInternalAnalystAuthorizationUseCase
```

---

# Example Input Ports

```java
interface AuthorizeOperationUseCase {

    void authorize(
        User user,
        Operation operation
    );
}
```

```java
interface AuthorizeBankAccountOperationUseCase {

    void authorize(
        User user,
        BankAccount account,
        Operation operation
    );
}
```

```java
interface AuthorizeLoanApprovalUseCase {

    void authorize(
        User user,
        Loan loan
    );
}
```

```java
interface AuthorizeTransferApprovalUseCase {

    void authorize(
        User user,
        Transfer transfer
    );
}
```

The exact return type may be defined by the authorization Domain Model or exception strategy adopted by the project.

---

# Authorization Flow

## General Flow

```text
Authenticated User
        │
        ▼
      User
        │
        ▼
Authorization Service
        │
        ├── Validate User Status
        │
        ├── Validate SystemRole
        │
        ├── Validate Domain Relationships
        │
        ├── Query Output Ports if necessary
        │
        ▼
Authorization Decision
        │
        ├── Authorized
        │       │
        │       ▼
        │   Business Service
        │
        └── Unauthorized
                │
                ▼
        Authorization Exception
```

---

# Authorization for Bank Account Operations

```text
User
 │
 ▼
Authorization Service
 │
 ├── UserStatus
 ├── SystemRole
 ├── BankAccount
 ├── Ownership
 │
 └── BankAccountRepository
          │
          ▼
Authorization Decision
```

The actual account operation is subsequently executed by the Bank Account service.

---

# Authorization for Loan Operations

```text
User
 │
 ▼
Authorization Service
 │
 ├── UserStatus
 ├── SystemRole
 ├── Loan
 │
 └── LoanRepository
          │
          ▼
Authorization Decision
```

The Loan service remains responsible for the actual loan operation.

---

# Authorization for Transfer Operations

```text
User
 │
 ▼
Authorization Service
 │
 ├── UserStatus
 ├── SystemRole
 ├── Transfer
 ├── Customer relationship
 │
 └── TransferRepository
          │
          ▼
Authorization Decision
```

The Transfer service remains responsible for executing the transfer.

---

# Authorization Exceptions

Conceptual exceptions for this subdomain include:

```text
UnauthorizedOperationException
UserNotAuthorizedException
UserInactiveException
UserBlockedException
InsufficientRoleException
InvalidAuthorizationContextException
UnauthorizedProductOperationException
UnauthorizedCustomerOperationException
UnauthorizedTransferApprovalException
UnauthorizedLoanApprovalException
```

The complete exception catalog should be defined separately in the Domain Exceptions documentation.

---

# Separation from Authentication

The following responsibilities belong to Authentication:

```text
Validate username
Validate password
Load User by username
Generate JWT
```

The following responsibilities belong to Authorization:

```text
Validate User status
Validate SystemRole
Validate business permissions
Validate customer relationships
Validate product authorization
Authorize business operations
Authorize approval operations
```

Conceptually:

```text
              Authentication
                    │
             username/password
                    │
                    ▼
                  User
                    │
                    ▼
              Authorization
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
      BankAccount  Loan    Transfer
```

---

# Operation and Audit Integration

Authorization itself does not represent the successful execution of a business operation.

The originating business service is responsible for registering the operation after the authorized business action occurs.

For example:

```text
Transfer Request
      │
      ▼
Authorization
      │
      ▼
Transfer Service
      │
      ├── Execute Transfer
      │
      ▼
Operation
      │
      ▼
AuditLog
```

An authorization failure may also be logged when required by the system's security/audit rules.

If such an event must be recorded, the authorization service must use the appropriate Operation and Audit Output Ports rather than accessing persistence directly.

---

# Architectural Constraints

The following rules are mandatory for the Authorization subdomain:

1. Authorization is separate from authentication.
2. Authentication determines the identity of the user.
3. Authorization determines whether the authenticated user may perform an operation.
4. Authorization services must receive Domain Models.
5. Authorization services must never receive primitive identifiers as substitutes for Domain Models.
6. `User` must be used instead of a raw `userId`.
7. `SystemRole` must be obtained from `User.role`.
8. Roles must never be represented as arbitrary strings inside authorization services.
9. `BankingProduct` must be used instead of a raw product identifier when the authorization context is a banking product.
10. `BankAccount`, `Loan`, and `Transfer` must be used as their respective Domain Models when product-specific authorization is required.
11. Customer relationships must be represented through Domain Models.
12. User relationships with customers must use the Domain relationship represented by `User`.
13. User status must be evaluated using `User.status`.
14. Authorization services must validate Domain information directly whenever it is already available.
15. External information must be obtained through Output Ports.
16. Authorization services must never access databases directly.
17. Authorization services must never access MySQL directly.
18. Authorization services must never access MongoDB directly.
19. Authorization services must never access JPA, SQL, or persistence repositories directly.
20. Output Ports must be owned by the Domain.
21. Adapters implement Output Ports.
22. JWT implementation details must remain outside the Domain.
23. Authorization services must not validate passwords.
24. Authorization services must not generate JWT tokens.
25. Authorization services must not execute banking operations.
26. Business services remain responsible for their own business rules.
27. Authorization must occur before protected business operations are executed.
28. Loan approval authorization must be separate from the actual Loan approval operation.
29. Transfer approval authorization must be separate from the actual Transfer approval operation.
30. Authorization failures must result in an appropriate Domain/Application authorization exception.
31. The Domain must remain independent of REST and security frameworks.
32. The Authorization subdomain must be fully testable without requiring infrastructure components.

```
```
