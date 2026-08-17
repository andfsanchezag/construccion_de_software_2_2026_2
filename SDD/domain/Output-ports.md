# Output Ports

## Introduction

Output Ports define the contracts through which the Domain communicates with external resources.

The Domain owns these interfaces. Domain Services must never depend directly on:

- MySQL
- MongoDB
- JPA
- Spring
- HTTP
- REST
- external APIs
- persistence repositories

When a Domain Service requires information or functionality outside the Domain, it must use the corresponding Output Port.

The implementations of these ports belong to the Adapter layer.

---

# Architectural Rule

The dependency flow must always be:

```text
Domain Service
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

For example:

```text
LoanService
      |
      v
LoanRepositoryPort
      |
      v
LoanMySqlAdapter
      |
      v
MySQL
```

For auditing:

```text
OperationAuditService
      |
      v
AuditLogRepositoryPort
      |
      v
AuditLogMongoAdapter
      |
      v
MongoDB
```

---

# General Parameter Rule

Output Port methods must work with Domain Models.

They must not receive DTOs, persistence entities, or primitive identifiers when the corresponding Domain Model already exists.

Incorrect:

```java
Optional<Customer> findById(String customerId);
```

Correct:

```java
Optional<Customer> findByIdentification(Customer customer);
```

Incorrect:

```java
void updateAccount(String accountId, BigDecimal balance);
```

Correct:

```java
void update(BankAccount account);
```

This keeps the Domain independent from persistence and transport representations.

---

# Output Ports

## 1. CustomerRepositoryPort

### Responsibility

Provides the Domain with persistence and query capabilities for `Customer`.

### Methods

```java
public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Optional<Customer> findByIdentification(Customer customer);

    Optional<Customer> findByEmail(Customer customer);

    boolean existsByIdentification(Customer customer);

    boolean existsByEmail(Customer customer);

    List<Customer> findAll();

    void update(Customer customer);
}
```

### Main Consumers

- Customer Services
- User Authentication Services
- Authorization Services
- Bank Account Services
- Loan Services
- Transfer Services

---

# 2. UserRepositoryPort

### Responsibility

Provides persistence and query capabilities for `User`.

### Methods

```java
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByUsername(User user);

    Optional<User> findById(User user);

    boolean existsByUsername(User user);

    void update(User user);
}
```

### Main Consumers

- User Registration Services
- User Authentication Services
- Authorization Services
- Operation Services
- Audit Services

---

# 3. BankAccountRepositoryPort

### Responsibility

Provides persistence and query capabilities for `BankAccount`.

### Methods

```java
public interface BankAccountRepositoryPort {

    BankAccount save(BankAccount account);

    Optional<BankAccount> findByIdentifier(BankAccount account);

    List<BankAccount> findByOwner(Customer customer);

    boolean existsForOwner(BankAccount account);

    void update(BankAccount account);
}
```

### Main Consumers

- Bank Account Services
- Transfer Services
- Loan Services
- Authorization Services
- Operation Services

---

# 4. LoanRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Loan`.

### Methods

```java
public interface LoanRepositoryPort {

    Loan save(Loan loan);

    Optional<Loan> findByIdentifier(Loan loan);

    List<Loan> findByApplicant(Customer customer);

    List<Loan> findByStatus(Loan loan);

    void update(Loan loan);
}
```

### Main Consumers

- Loan Services
- Authorization Services
- Operation Services
- Audit Services

---

# 5. TransferRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Transfer`.

### Methods

```java
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

### Main Consumers

- Transfer Services
- Authorization Services
- Operation Services
- Audit Services

---

# 6. OperationRepositoryPort

### Responsibility

Provides persistence for business `Operation` records.

`Operation` represents the business action executed by the system.

### Methods

```java
public interface OperationRepositoryPort {

    Operation save(Operation operation);

    Optional<Operation> findById(Operation operation);

    List<Operation> findByUser(User user);

    List<Operation> findByProduct(BankingProduct product);

    List<Operation> findByType(Operation operation);
}
```

### Main Consumers

- Operation Services
- Audit Services
- Domain Services that execute auditable business operations

---

# 7. AuditLogRepositoryPort

### Responsibility

Provides persistence for immutable audit records.

The implementation is expected to use MongoDB, while the Domain remains completely unaware of MongoDB.

### Methods

```java
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByUser(User user);

    List<AuditLog> findByProduct(BankingProduct product);

    List<AuditLog> findByOperationType(AuditLog auditLog);
}
```

### Main Consumers

- Operation and Audit Services
- Domain Services that generate auditable events

---

# 8. PasswordServicePort

### Responsibility

Abstracts password hashing and password verification.

The Domain must not depend directly on BCrypt, Argon2, Spring Security, or another password implementation.

### Methods

```java
public interface PasswordServicePort {

    boolean matches(User user);

    String encrypt(User user);
}
```

### Main Consumers

- User Registration Services
- User Authentication Services

### Architectural Rule

The concrete implementation belongs outside the Domain.

For example:

```text
PasswordServicePort
        ^
        |
PasswordSecurityAdapter
        |
        v
Password Hashing Library
```

---

# 9. JwtServicePort

### Responsibility

Abstracts JWT generation from the Domain.

The Domain must not depend directly on JWT libraries or security frameworks.

### Methods

```java
public interface JwtServicePort {

    String generateToken(User user);
}
```

### Main Consumer

- User Authentication Services

### JWT Rule

The JWT must not contain the user's password.

The token may contain claims such as:

```text
username
role
```

and other claims strictly required by the application.

---

# 10. NotificationPort

### Responsibility

Abstracts communication with external notification systems.

Possible channels include:

- Email
- SMS
- Push Notification

### Preferred Method

If a `Notification` Domain Model exists, prefer:

```java
public interface NotificationPort {

    void send(Notification notification);
}
```

This is preferable to exposing transport-specific parameters such as email addresses, phone numbers, or message formats directly in the Domain.

### Main Consumers

- Customer Services
- Loan Services
- Transfer Services
- User Services

---

# 11. AuthorizationPort

### Responsibility

Provides external authorization information only when the authorization decision cannot be made using information already contained in the Domain Models.

### Methods

```java
public interface AuthorizationPort {

    boolean isAuthorized(User user, Customer customer);

    boolean canOperateOn(User user, BankingProduct product);

    boolean canApprove(User user, BankingProduct product);
}
```

### Important Rule

This port must not replace normal Domain validation.

For example, if the authorization depends only on:

```java
user.getRole()
```

the Domain Service should evaluate that directly.

There is no reason to call `AuthorizationPort` simply to determine whether:

```text
INTERNAL_ANALYST
```

can approve a loan if that rule is already part of the Domain.

The port is reserved for information that must come from outside the Domain.

---

# 12. BusinessConfigurationPort

### Responsibility

Provides configurable business parameters that are external to the Domain entities.

Examples include:

- Transfer approval threshold
- Transfer approval expiration period
- Other business parameters explicitly defined as configurable

### Methods

```java
public interface BusinessConfigurationPort {

    BigDecimal getTransferApprovalThreshold();

    Integer getTransferApprovalExpirationMinutes();
}
```

### Main Consumers

- Transfer Services
- Authorization Services
- Other services requiring externally configurable business rules

### Example

Instead of hardcoding:

```java
if (transfer.getAmount().compareTo(new BigDecimal("10000000")) > 0) {
    ...
}
```

the Domain Service can use:

```text
TransferService
      |
      v
BusinessConfigurationPort
      |
      v
Transfer Approval Threshold
```

---

# Port Organization

The Domain package should contain:

```text
domain/
└── ports/
    ├── in/
    │
    └── out/
        ├── CustomerRepositoryPort
        ├── UserRepositoryPort
        ├── BankAccountRepositoryPort
        ├── LoanRepositoryPort
        ├── TransferRepositoryPort
        ├── OperationRepositoryPort
        ├── AuditLogRepositoryPort
        ├── PasswordServicePort
        ├── JwtServicePort
        ├── NotificationPort
        ├── AuthorizationPort
        └── BusinessConfigurationPort
```

---

# Mapping to Adapters

The output adapters implement the Domain Ports.

```text
domain/ports/out/
        |
        +-- CustomerRepositoryPort
        |       ^
        |       |
        |   CustomerMySqlAdapter
        |
        +-- UserRepositoryPort
        |       ^
        |       |
        |   UserMySqlAdapter
        |
        +-- BankAccountRepositoryPort
        |       ^
        |       |
        |   BankAccountMySqlAdapter
        |
        +-- LoanRepositoryPort
        |       ^
        |       |
        |   LoanMySqlAdapter
        |
        +-- TransferRepositoryPort
        |       ^
        |       |
        |   TransferMySqlAdapter
        |
        +-- OperationRepositoryPort
        |       ^
        |       |
        |   OperationMySqlAdapter
        |
        +-- AuditLogRepositoryPort
        |       ^
        |       |
        |   AuditLogMongoAdapter
        |
        +-- PasswordServicePort
        |       ^
        |       |
        |   PasswordSecurityAdapter
        |
        +-- JwtServicePort
        |       ^
        |       |
        |   JwtSecurityAdapter
        |
        +-- NotificationPort
        |       ^
        |       |
        |   NotificationAdapter
        |
        +-- AuthorizationPort
        |       ^
        |       |
        |   AuthorizationAdapter
        |
        +-- BusinessConfigurationPort
                ^
                |
            ConfigurationAdapter
```

---

# Database Responsibility

A Port does not necessarily correspond one-to-one with a physical database table.

For example:

```text
CustomerRepositoryPort
        |
        v
CustomerMySqlAdapter
        |
        +-- customer table
        +-- natural_customer table
        +-- business_customer table
```

The Domain only knows:

```text
CustomerRepositoryPort
```

It does not know how many tables are used to persist `Customer`.

Therefore, the physical database design can evolve without changing the Domain.

---

# Service-to-Port Relationship

## Customer Services

Typically depend on:

```text
CustomerRepositoryPort
UserRepositoryPort
AuthorizationPort
NotificationPort
OperationRepositoryPort
AuditLogRepositoryPort
```

---

## User Authentication Services

Typically depend on:

```text
UserRepositoryPort
PasswordServicePort
JwtServicePort
```

---

## Bank Account Services

Typically depend on:

```text
BankAccountRepositoryPort
CustomerRepositoryPort
AuthorizationPort
OperationRepositoryPort
AuditLogRepositoryPort
```

---

## Loan Services

Typically depend on:

```text
LoanRepositoryPort
CustomerRepositoryPort
BankAccountRepositoryPort
AuthorizationPort
OperationRepositoryPort
AuditLogRepositoryPort
NotificationPort
```

---

## Transfer Services

Typically depend on:

```text
TransferRepositoryPort
BankAccountRepositoryPort
CustomerRepositoryPort
AuthorizationPort
BusinessConfigurationPort
OperationRepositoryPort
AuditLogRepositoryPort
NotificationPort
```

---

## Authorization Services

May depend on:

```text
AuthorizationPort
CustomerRepositoryPort
UserRepositoryPort
BankAccountRepositoryPort
LoanRepositoryPort
TransferRepositoryPort
```

Only use the repository ports when authorization requires information not already available in the supplied Domain Models.

---

## Operation and Audit Services

Depend on:

```text
OperationRepositoryPort
AuditLogRepositoryPort
```

---

# Final Architectural Rules

The following rules are mandatory:

1. All Output Ports belong to the Domain.
2. Output Ports are interfaces.
3. Adapters implement Output Ports.
4. Domain Services never access repositories directly.
5. Domain Services never access databases directly.
6. Domain Services never access HTTP or REST directly.
7. Domain Services never depend on Spring, JPA, MongoDB, or MySQL.
8. Ports must use Domain Models rather than DTOs.
9. Do not represent Domain relationships using primitive IDs when a Domain Model can represent the relationship.
10. Use Output Ports only when the required information or capability is external to the Domain.
11. Do not create a Port merely because a physical database contains another table.
12. Business rules that can be evaluated from Domain Models must remain inside the Domain.
13. External/configurable business information must be accessed through an appropriate Output Port.
14. Persistence adapters translate between Domain Models and persistence representations.
15. The Domain must remain fully testable without a database or external infrastructure.
