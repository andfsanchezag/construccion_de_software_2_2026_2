# Customer Services

## Introduction

This document defines the services belonging to the **Customer Management** subdomain of the Banking Information Management System.

The services in this subdomain are responsible for the creation, consultation, modification, and lifecycle management of banking customers.

The services operate exclusively with **Domain Models** and **Value Objects**. They must not depend directly on databases, persistence entities, repositories, frameworks, HTTP, REST, or other infrastructure technologies.

When a service requires information that is not available in the Domain Models involved in the current operation, the service must obtain that information through an **Output Port**.

Output Ports are interfaces owned by the Domain layer and implemented by Output Adapters.

---

# Architectural Principles

## Domain Model Parameters

All Customer Services and their corresponding Input Ports must receive **Domain Models or Value Objects** as parameters.

Services must never receive:

- `String` identifiers as substitutes for domain relationships.
- Primitive identifiers.
- Individual attributes that belong to a Domain Model.
- Request DTOs.
- Persistence entities.

For example, the following approach is not allowed:

```java
registerNaturalCustomer(
    String identification,
    String name,
    String email
);
````

The service must instead receive the corresponding Domain Model:

```java
registerNaturalCustomer(
    NaturalCustomer customer
);
```

Likewise, the following is not allowed:

```java
changeCustomerStatus(
    String customerId,
    CustomerStatus status
);
```

The service must operate on the Domain Model:

```java
changeCustomerStatus(
    Customer customer
);
```

The same principle applies to relationships between domain entities.

For example, a business customer must contain:

```text
BusinessCustomer
    │
    └── legalRepresentative : NaturalCustomer
```

and not:

```text
BusinessCustomer
    │
    └── legalRepresentativeId : String
```

---

## External Information

A service may require information that is not available in the Domain Model received as a parameter.

In these cases, the service must obtain the required information through an Output Port.

The service must never access a database directly.

The interaction follows:

```text
Domain Model
     │
     ▼
Customer Service
     │
     ├── Domain validation
     │
     └── Output Port
             │
             ▼
       Output Adapter
             │
             ▼
          Database
```

The Output Port exposes domain-oriented operations and must not expose database-specific concepts.

---

# Customer Services

The Customer Management subdomain contains the following services:

1. Register Natural Customer
2. Register Business Customer
3. Consult Customer
4. Update Customer
5. Change Customer Status
6. Consult Customer Products

---

# 1. Register Natural Customer

## Description

Creates a new `NaturalCustomer` in the banking domain.

The service constructs and validates the customer according to the business rules defined by the domain.

The service receives a complete `NaturalCustomer` Domain Model rather than individual customer attributes.

---

## Input

```text
NaturalCustomer
```

The model contains the information required to represent the customer, including:

* Identification
* Name
* Email
* Phone number
* Address
* Birth date
* Role
* Customer status

The exact attributes are defined by the Domain Model.

---

## Domain Validation

### Age Validation

The natural customer must be at least 18 years old.

The validation uses:

```text
NaturalCustomer.birthDate
```

and the current date.

This validation belongs entirely to the domain and does not require an Output Port.

Conceptually:

```text
birthDate
    │
    ▼
Current Date
    │
    ▼
Age >= 18
```

---

### Identification Validation

The customer's identification must be unique.

Uniqueness cannot be determined from the `NaturalCustomer` object alone because it depends on previously persisted customers.

Therefore, the service must use the appropriate Output Port.

Conceptually:

```text
NaturalCustomer
      │
      └── identification
               │
               ▼
       Customer Output Port
               │
               ▼
       Existing Customer?
```

If another customer already has the same identification, the service must reject the registration.

---

### Customer Status Validation

The initial `CustomerStatus` must represent a valid state according to the Domain Model.

The service must not use `UserStatus` for this validation.

Customer state and system-user state are independent concepts.

---

## Persistence

Once the domain validations succeed, the service persists the customer through an Output Port.

Conceptually:

```text
NaturalCustomer
      │
      ▼
CustomerRepository
      │
      ▼
Persistence Adapter
      │
      ▼
Customer Storage
```

The service must not know whether the adapter uses MySQL, another relational database, or another persistence mechanism.

---

# 2. Register Business Customer

## Description

Creates a new `BusinessCustomer` representing a legal business entity.

The service establishes the business customer's information and its relationship with the legal representative.

---

## Input

```text
BusinessCustomer
```

The model contains the business information defined by the Domain Model.

The legal representative is represented as a domain relationship:

```text
BusinessCustomer
    │
    └── legalRepresentative : NaturalCustomer
```

The service must not receive a separate `legalRepresentativeId : String`.

---

## Domain Validation

### Business Identification Validation

The business identification must be unique.

Because uniqueness depends on persisted information, the service must use an Output Port to verify that another customer does not already use the identification.

---

### Legal Representative Validation

The business customer must have a valid legal representative.

The legal representative must be represented by a `NaturalCustomer`.

If the complete `NaturalCustomer` is already available in the received `BusinessCustomer`, the service can perform the applicable domain validations directly.

If additional information is required, the service must obtain it through an Output Port.

---

### Legal Representative State

The service must validate the conditions required by the domain for a natural customer to act as legal representative.

These validations must use the attributes and state of the `NaturalCustomer` Domain Model.

If information outside the available model is required, the service must obtain it through an Output Port.

---

## Persistence

After successful validation, the service persists the `BusinessCustomer` through the customer Output Port.

The persistence adapter is responsible for translating the domain relationship into the database representation.

The domain itself continues to represent the relationship as:

```text
BusinessCustomer
    │
    └── legalRepresentative : NaturalCustomer
```

---

# 3. Consult Customer

## Description

Retrieves a customer from the system and returns the corresponding Domain Model.

The service does not expose persistence entities or database-specific representations.

---

## Input

The service receives a Domain Model representing the customer being consulted.

When the customer is not yet available in memory, the application boundary must provide the domain representation required by the service.

The service must not be defined as:

```java
consultCustomer(String customerId);
```

when the identifier represents a domain relationship.

The service must operate on:

```text
Customer
```

or the appropriate Domain Model required by the use case.

---

## Processing

The service validates the received customer information and obtains any required external information through an Output Port.

Conceptually:

```text
Customer
   │
   ▼
Customer Service
   │
   ├── Domain validation
   │
   └── Output Port
          │
          ▼
     Customer Data
```

---

## Customer Existence

If the operation requires retrieving the persistent state of the customer, the service must use the customer Output Port.

If the customer cannot be found, the service must raise the corresponding domain exception.

---

## Authorization

The service must verify that the requesting user is allowed to access the customer information.

The authorization rules are:

* `TELLER_EMPLOYEE`: may consult any customer.
* `COMMERCIAL_EMPLOYEE`: may consult **any customer without restriction**.
* `INTERNAL_ANALYST`: may consult any customer.
* `NATURAL_CUSTOMER`: may consult only their own customer record.
* `BUSINESS_OPERATOR` / `BUSINESS_SUPERVISOR`: may consult only the customer associated with their `User.customer`.

Authorization must be evaluated using Domain Models and Value Objects.

If external information is necessary, the service must obtain it through an Output Port.

Authorization rules must not be implemented in controllers.

---

## Output

The service returns:

```text
Customer
```

which may represent:

```text
NaturalCustomer
```

or:

```text
BusinessCustomer
```

depending on the customer being consulted.

---

# 4. Update Customer

## Description

Updates information belonging to an existing customer.

The service receives the customer as a Domain Model and applies the appropriate domain changes.

It must not receive individual attributes as separate parameters.

---

## Input

```text
Customer
```

The received model represents the customer and the desired domain state.

---

## Processing

```text
Customer
   │
   ▼
Validate Domain State
   │
   ▼
Apply Domain Changes
   │
   ▼
Validate Business Rules
   │
   ▼
Customer Output Port
```

---

## Domain Validation

### Customer Existence

The customer must exist in the system before it can be updated.

If persistence information is required, the service must use the Customer Output Port.

---

### Updated Information

The resulting Domain Model must satisfy all domain constraints.

Examples include:

* Valid identification.
* Valid name.
* Valid email.
* Valid phone number.
* Valid address.
* Valid customer status.
* Valid relationships with other domain entities.

The exact validation rules belong to the Domain Model and Value Objects.

---

### Identification Uniqueness

If identification is allowed to change, the new identification must remain unique.

This cannot be determined exclusively from the Domain Model.

The service must use an Output Port to verify uniqueness against persisted customers.

---

### Authorization

The user performing the operation must have permission to update the customer.

Authorization must use Domain Models and Value Objects whenever the required information is available in the domain.

External information must be obtained through Output Ports.

---

## Persistence

After the domain operation succeeds, the resulting `Customer` is persisted through the appropriate Output Port.

The service must not manipulate persistence entities or database records directly.

---

# 5. Change Customer Status

## Description

Changes the `CustomerStatus` of an existing customer.

Customer status represents the state of the customer's banking relationship and is independent of the status of a system `User`.

---

## Input

```text
Customer
```

The new state must be represented within the Domain Model or through a domain operation that changes the customer's status.

The service must not receive:

```java
changeCustomerStatus(
    String customerId,
    CustomerStatus status
);
```

The service must operate on the customer Domain Model.

---

## Processing

```text
Customer
   │
   ▼
Validate Current State
   │
   ▼
Validate Status Transition
   │
   ▼
Change CustomerStatus
   │
   ▼
Persist Customer
```

---

## Domain Validation

### Current Status

The service must evaluate the customer's current `CustomerStatus`.

---

### Status Transition

The domain determines whether the requested transition is valid.

For example:

```text
ACTIVE → BLOCKED
ACTIVE → INACTIVE
BLOCKED → ACTIVE
```

The exact allowed transitions belong to the domain rules.

The database must never determine whether a state transition is valid.

---

### User Status Independence

Changing the customer status must not automatically change `UserStatus`.

For example:

```text
CustomerStatus = BLOCKED
```

does not necessarily imply:

```text
UserStatus = BLOCKED
```

These represent different business concepts.

---

## Persistence

The updated customer is persisted through the Customer Output Port.

---

# 6. Consult Customer Products

## Description

Retrieves the banking products and services associated with a customer.

The result may include:

```text
BankAccount
Loan
Transfer
```

All products are returned as Domain Models.

---

## Input

```text
Customer
```

The service must receive the customer Domain Model and must not receive only:

```text
customerId : String
```

when the identifier is being used as a substitute for the domain relationship.

---

## Processing

The service verifies the customer and retrieves the associated products through their corresponding Output Ports.

Conceptually:

```text
                         Customer
                            │
                            ▼
                  Customer Product Service
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
     BankAccountPort    LoanPort      TransferPort
             │              │              │
             └──────────────┼──────────────┘
                            ▼
                     Domain Products
```

---

## Product Relationships

The resulting relationships remain represented through Domain Models:

```text
Customer
   │
   ├── BankAccount
   │
   ├── Loan
   │
   └── Transfer
```

The service must not construct relationships such as:

```text
customerId
accountId
loanId
transferId
```

as replacements for domain relationships.

Identifiers may be used internally by persistence adapters to locate records, but they must not replace Domain Model relationships in the business layer.

---

## Output Ports

The service may require the following Output Ports:

```text
BankAccountRepository
LoanRepository
TransferRepository
```

These interfaces belong to:

```text
domain/ports/out/
```

and their implementations belong to the appropriate persistence adapters.

---

# Output Ports

Customer Services communicate with external resources exclusively through Output Ports.

The following ports are relevant to this subdomain.

---

## CustomerRepository

Provides the persistence capabilities required by Customer Services.

Conceptual responsibilities include:

```text
save(Customer)
find(Customer)
exists(Customer)
```

The exact methods should be defined according to the use cases and should receive Domain Models or Value Objects rather than persistence entities.

For example:

```java
interface CustomerRepository {

    Customer save(Customer customer);

    boolean exists(Customer customer);

    Customer find(Customer customer);
}
```

The exact method signatures are implementation decisions and may be refined in the detailed port documentation.

---

## BankAccountRepository

Provides access to `BankAccount` Domain Models when Customer Services need to retrieve products associated with a customer.

---

## LoanRepository

Provides access to `Loan` Domain Models when Customer Services need to retrieve products associated with a customer.

---

## TransferRepository

Provides access to `Transfer` Domain Models when Customer Services need to retrieve products associated with a customer.

---

# Service and Port Interaction

The dependency direction must remain:

```text
                 Domain
                    │
          ┌─────────┴─────────┐
          │                   │
   Customer Services     Output Ports
                              │
                              ▼
                       Output Adapters
                              │
                              ▼
                          Database
```

The service depends on the interface:

```text
CustomerRepository
```

and never on:

```text
MySQL
JPA
Spring Data
SQL
Repository Implementation
```

The adapter implements the port:

```text
CustomerRepository
        ▲
        │
        │ implements
        │
CustomerPersistenceAdapter
```

---

# Validation Strategy

All Customer Services must follow this validation strategy.

## Domain-Available Information

If the required information is already present in the Domain Model, the validation must be performed directly in the Domain.

Examples:

```text
NaturalCustomer.birthDate
Customer.status
BusinessCustomer.legalRepresentative
Customer.role
```

No Output Port should be used merely to validate information already contained in the domain object.

---

## External Information

If the validation requires information outside the Domain Model, the service must use an Output Port.

Examples:

```text
Identification uniqueness
Customer existence
Existing customer relationships
Persisted customer products
```

Conceptually:

```text
             Validation Required
                     │
            ┌────────┴────────┐
            │                 │
       Domain data       External data
            │                 │
            ▼                 ▼
     Domain validation   Output Port
                              │
                              ▼
                       External Resource
```

---

# Domain Model Rule

All services in this subdomain must comply with the following rule:

> **Service methods must receive Domain Models or Value Objects as parameters. They must never receive primitive values, Strings, identifiers, Request DTOs, persistence entities, or isolated attributes that represent part of a Domain Model.**

For example:

### Incorrect

```java
registerNaturalCustomer(
    String identification,
    String name,
    String email
);
```

### Correct

```java
registerNaturalCustomer(
    NaturalCustomer customer
);
```

---

### Incorrect

```java
registerBusinessCustomer(
    BusinessCustomer customer,
    String legalRepresentativeId
);
```

### Correct

```java
registerBusinessCustomer(
    BusinessCustomer customer
);
```

with:

```text
BusinessCustomer
    │
    └── legalRepresentative : NaturalCustomer
```

---

### Incorrect

```java
consultCustomer(String customerId);
```

### Correct

```java
consultCustomer(Customer customer);
```

---

### Incorrect

```java
updateCustomer(
    String customerId,
    String email,
    String phone
);
```

### Correct

```java
updateCustomer(Customer customer);
```

---

# Input Port Rule

The same Domain Model parameter rule applies to Input Ports.

Input Ports represent use cases and must expose operations using Domain Models and Value Objects.

For example:

```java
interface RegisterNaturalCustomerUseCase {

    NaturalCustomer registerNaturalCustomer(
        NaturalCustomer customer
    );
}
```

The Input Adapter is responsible for converting external representations into Domain Models before invoking the Input Port.

The flow is:

```text
HTTP Request
      │
      ▼
Request DTO
      │
      ▼
Mapper
      │
      ▼
NaturalCustomer
      │
      ▼
Input Port
      │
      ▼
Customer Service
```

DTOs therefore never enter the Domain layer.

---

# Exceptions

Customer Services must use Domain Exceptions for business rule violations.

Conceptual examples include:

```text
CustomerAlreadyExistsException
CustomerNotFoundException
InvalidCustomerException
InvalidCustomerStatusException
InvalidLegalRepresentativeException
UnauthorizedCustomerOperationException
```

The exact exception catalog should be defined separately in the Domain Exceptions documentation.

---

# Operation and Audit Considerations

Customer registration, customer status changes, and other significant customer actions may generate `Operation` and `AuditLog` records when required by the business rules.

When an operation must be registered, Customer Services must not directly access the audit database.

The service must interact with the corresponding Domain Service or Output Port responsible for operation and audit management.

Conceptually:

```text
Customer Service
      │
      ▼
Business Operation
      │
      ├── Operation
      │
      └── AuditLog
```

The exact operation and audit flow will be defined in the **Operation and Audit** subdomain documentation.

---

# Architectural Constraints

The following constraints are mandatory for all Customer Services:

1. Business logic belongs exclusively to the Domain layer.
2. Services must operate on Domain Models and Value Objects.
3. Services must never receive REST Request DTOs.
4. Services must never receive primitive identifiers as substitutes for Domain Model relationships.
5. Services must never receive isolated attributes that belong to a Domain Model.
6. Services must never depend directly on databases.
7. Services must always use Output Ports when external information is required.
8. Output Ports are interfaces owned by the Domain layer.
9. Output Adapters implement Output Ports.
10. Persistence entities must never enter the Domain layer.
11. Controllers must never contain Customer business rules.
12. Domain validations must not be delegated to database implementations.
13. Domain relationships must be represented using Domain Models.
14. Customer status and User status must remain independent concepts.
15. The Domain must remain independent of Spring, JPA, MySQL, MongoDB, HTTP, REST, JSON, and SQL.
16. Input Ports must follow the same Domain Model parameter rule as Domain Services.
17. External representations must be converted into Domain Models by Input Adapters before entering the Domain.
18. Customer Services must remain testable without requiring database or infrastructure components.

```
```
