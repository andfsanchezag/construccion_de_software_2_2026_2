# Customer Services

## 1. Purpose

This document defines the application/domain services belonging to the **Customer Management** subdomain of the Banking Information Management System.

The services are responsible for:

1. Registering natural customers.
2. Registering business customers.
3. Consulting customers.
4. Updating customer information.
5. Changing customer status.
6. Consulting products associated with a customer.

The specification follows the established **DDD + Hexagonal Architecture (Ports & Adapters)** principles and uses the **Loan Services service pattern** as the structural reference.

All services operate on Domain Models and Value Objects. Infrastructure concerns are accessed exclusively through Output Ports.

---

# 2. Architectural Principles

## 2.1 Domain-first

Business rules belong to the Domain layer.

Customer Services coordinate the execution of use cases but must not contain infrastructure-specific logic.

The Domain must remain independent of:

- Spring
- Spring Data
- JPA
- Hibernate
- MySQL
- PostgreSQL
- MongoDB
- SQL
- HTTP
- REST
- JSON
- Persistence entities
- Controllers
- Infrastructure adapters

---

## 2.2 Domain Model Parameters

Customer Services and their Input Ports must receive Domain Models or Value Objects.

They must never receive:

- `String` identifiers as substitutes for domain relationships.
- Primitive identifiers.
- Individual attributes belonging to a Domain Model.
- REST Request DTOs.
- Persistence entities.

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

Likewise:

### Incorrect

```java
changeCustomerStatus(
    String customerId,
    CustomerStatus status
);
```

### Correct

```java
changeCustomerStatus(
    Customer customer
);
```

The requested state must therefore be represented by the Domain Model or by a domain operation/value object defined by the domain model.

---

## 2.3 Domain Relationships

Domain relationships must be represented using Domain Models.

For example:

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

Identifiers may exist inside Value Objects or be used by adapters to locate persistence records, but they must not replace domain relationships in the business layer.

---

## 2.4 External Information

When a validation or operation requires information that is not available in the Domain Model currently being processed, the service must use an Output Port.

The service must never access a database directly.

```text
Domain Model
     │
     ▼
Customer Service
     │
     ├── Domain behavior / validation
     │
     └── Output Port
             │
             ▼
       Output Adapter
             │
             ▼
       External Resource
```

Output Ports expose domain-oriented operations and must not expose:

- SQL queries
- JPA repositories
- database connections
- persistence entities
- table names
- infrastructure-specific objects

---

# 3. Standard Customer Service Pattern

Every Customer Service must follow the same high-level execution pattern whenever the use case changes or performs a significant business action:

```text
Validate Input Domain Model
        │
        ▼
Load Required Persistent State
        │
        ▼
Validate Existence / Relationships
        │
        ▼
Authorize Actor
        │
        ▼
Execute Domain Behavior
        │
        ▼
Persist Domain Model
        │
        ▼
Register Operation
        │
        ▼
Register AuditLog
        │
        ▼
Return Domain Model
```

For read-only services, the persistence, Operation, and Audit steps are only included when required by the business rules.

The service must not register an Operation or AuditLog when the business action fails.

---

# 4. Service Contract Standard

Every service in this document defines, where applicable:

- Description
- Actor
- Context
- Input Domain Model
- Input Port
- Preconditions
- Existence validation
- Related entity validation
- Ownership/relationship validation
- Authorization
- Domain validation
- Domain behavior
- State change
- Persistence
- Operation
- Audit
- Output
- Exceptions
- Output Ports
- Processing flow

This makes every use case independently implementable and testable.

---

# 5. Customer Service Validation Strategy

## 5.1 Domain-available information

If the required information is already contained in a Domain Model or Value Object, validation must be performed using domain behavior.

Examples:

```text
NaturalCustomer.birthDate
Customer.customerStatus
Customer.role
BusinessCustomer.legalRepresentative
```

No Output Port should be called merely to validate information already available in the domain.

---

## 5.2 External information

If the validation depends on persisted or external information, the service must use an Output Port.

Examples:

- Customer existence.
- Identification uniqueness.
- Existence of a legal representative in persistence.
- Existing customer relationships.
- Persisted banking products.
- Actor/customer relationship when it cannot be established from the supplied domain objects.

---

# 6. Service Validation Matrix

| Service | Actor / Context | Existence | Related Entities | Authorization | Domain Mutation | Persistence | Operation | Audit |
|---|---|---|---|---|---|---|---|---|
| Register Natural Customer | Customer or authorized banking employee, according to registration policy | Identification must not already exist | None | Required | Create customer | Yes | Yes | Yes |
| Register Business Customer | Customer or authorized banking employee, according to registration policy | Business identification must not already exist | Legal representative must exist and be valid | Required | Create customer | Yes | Yes | Yes |
| Consult Customer | Authenticated actor | Customer must exist | Ownership/association when applicable | Required | No | Read | No, unless audit policy requires | Usually no; if required, audit only |
| Update Customer | Authorized actor | Customer must exist | Relationships must remain valid | Required | Update allowed fields | Yes | Yes | Yes |
| Change Customer Status | Authorized banking actor | Customer must exist | None unless domain requires | Required | Change status | Yes | Yes | Yes |
| Consult Customer Products | Authenticated actor | Customer must exist | Associated products | Required | No | Read | No, unless audit policy requires | Usually no; if required, audit only |

**Note:** Exact actor restrictions must remain aligned with the authorization domain. This document does not invent a new role model.

---

# 7. Register Natural Customer

## 7.1 Description

Creates and persists a new `NaturalCustomer`.

The service receives the complete Domain Model and validates all rules that can be determined from the domain object. Rules requiring persisted information are evaluated through Output Ports.

---

## 7.2 Actor

The actor must be an authenticated and authorized actor according to the customer-registration policy.

The service must not assume that every caller has permission to register customers.

Authorization must be performed by the authorization mechanism through an Output Port or Domain Authorization Service.

---

## 7.3 Context

The operation represents the creation of a new customer in the banking domain.

---

## 7.4 Input Port

```java
interface RegisterNaturalCustomerUseCase {

    NaturalCustomer registerNaturalCustomer(
        NaturalCustomer customer
    );
}
```

---

## 7.5 Input

```text
NaturalCustomer
```

The model contains the attributes defined by the Customer Domain Model, including the applicable:

- Identification
- Name
- Email
- Phone
- Address
- Birth date
- Role
- Customer status

The exact attribute set is defined by the Domain Model.

---

## 7.6 Preconditions

Before registration:

1. The supplied Domain Model must be structurally valid.
2. The customer must satisfy all NaturalCustomer domain invariants.
3. The actor must be authorized.
4. No persisted customer may violate identification uniqueness.
5. The initial customer status must be valid.

---

## 7.7 Domain Validation

### Age

The natural customer must be at least 18 years old.

The validation uses:

```text
NaturalCustomer.birthDate
+
Current Date
```

This is a domain validation.

The service must not obtain the customer's age from the database.

---

### Customer Status

The initial `CustomerStatus` must be a valid state.

`CustomerStatus` and `UserStatus` are independent concepts.

Registering a customer must not implicitly create or modify a system user's status.

---

### Other Domain Invariants

The resulting `NaturalCustomer` must satisfy all constraints defined by:

- Domain Model
- Value Objects
- Domain invariants

---

## 7.8 External Validation

### Identification Uniqueness

The service must verify that no existing customer already uses the same identification.

This requires persisted information and therefore uses:

```text
CustomerRepositoryPort
```

If the identification already exists:

```text
CustomerAlreadyExistsException
```

---

## 7.9 Authorization

The service must authorize the actor before persistence.

The authorization decision must consider the actor represented by the appropriate Domain Model/Value Objects.

Authorization must not be implemented in a controller.

---

## 7.10 Domain Behavior

The service must execute the domain behavior responsible for creating the customer.

Conceptually:

```java
NaturalCustomer registeredCustomer =
    customer.register();
```

The exact method name is an implementation detail and must follow the actual Domain Model.

The service must not duplicate domain invariants in application code.

---

## 7.11 Persistence

After all validations, authorization, and domain behavior succeed:

```text
NaturalCustomer
      │
      ▼
CustomerRepositoryPort
      │
      ▼
Customer Persistence Adapter
```

The persisted representation remains an infrastructure concern.

---

## 7.12 Operation

A successful customer registration is a business operation.

The service must register the corresponding `Operation` through the appropriate Output Port.

No Operation must be registered when registration fails.

---

## 7.13 Audit

A successful registration must produce the corresponding `AuditLog` when required by the banking audit policy.

The service must not access an audit database directly.

---

## 7.14 Output

```text
NaturalCustomer
```

The returned object is the resulting Domain Model.

---

## 7.15 Exceptions

Possible exceptions include:

- `InvalidCustomerException`
- `CustomerAlreadyExistsException`
- `UnauthorizedCustomerOperationException`
- Domain-specific Value Object validation exceptions

---

## 7.16 Output Ports

- `CustomerRepositoryPort`
- `AuthorizationPort`
- `OperationRepositoryPort`
- `AuditLogRepositoryPort`

---

## 7.17 Processing Flow

```text
NaturalCustomer
      │
      ▼
Validate Domain Model
      │
      ▼
Validate Age / Domain Invariants
      │
      ▼
Check Identification Uniqueness
      │
      ▼
Authorize Actor
      │
      ▼
Execute Customer Domain Behavior
      │
      ▼
Persist Customer
      │
      ▼
Register Operation
      │
      ▼
Register AuditLog
      │
      ▼
Return NaturalCustomer
```

---

# 8. Register Business Customer

## 8.1 Description

Creates and persists a new `BusinessCustomer`.

The legal representative is a domain relationship:

```text
BusinessCustomer
    │
    └── legalRepresentative : NaturalCustomer
```

The service must not receive a separate legal-representative identifier.

---

## 8.2 Actor

The actor must be authorized according to the customer-registration policy.

---

## 8.3 Context

The operation creates a business customer and establishes its relationship with a natural customer acting as legal representative.

---

## 8.4 Input Port

```java
interface RegisterBusinessCustomerUseCase {

    BusinessCustomer registerBusinessCustomer(
        BusinessCustomer customer
    );
}
```

---

## 8.5 Input

```text
BusinessCustomer
```

The model contains the business information defined by the Domain Model.

---

## 8.6 Preconditions

1. BusinessCustomer must be valid.
2. Business identification must be unique.
3. `legalRepresentative` must be present.
4. The legal representative must be a valid `NaturalCustomer`.
5. The legal representative must satisfy all domain conditions required to act in that role.
6. The actor must be authorized.

---

## 8.7 Related Entity Validation

The legal representative is represented directly by:

```text
NaturalCustomer
```

If the received model contains sufficient information, domain validations are performed directly.

If persistence information is required, the service uses:

```text
CustomerRepositoryPort
```

The service must not replace the relationship with:

```text
String legalRepresentativeId
```

---

## 8.8 External Validation

The service must verify:

- Business identification uniqueness.
- Required existence of the legal representative.
- Any persisted state required by the business rule.

---

## 8.9 Authorization

The actor must be authorized before the business customer is persisted.

---

## 8.10 Domain Behavior

The service executes the appropriate `BusinessCustomer` domain behavior to establish the valid customer state and relationship.

The exact domain method is defined by the Domain Model.

---

## 8.11 Persistence

```text
BusinessCustomer
      │
      ▼
CustomerRepositoryPort
      │
      ▼
Persistence Adapter
```

The adapter translates the domain relationship into its persistence representation.

---

## 8.12 Operation

Successful registration generates the corresponding `Operation`.

---

## 8.13 Audit

Successful registration generates the corresponding `AuditLog` according to the audit policy.

---

## 8.14 Output

```text
BusinessCustomer
```

---

## 8.15 Exceptions

Possible exceptions include:

- `InvalidCustomerException`
- `CustomerAlreadyExistsException`
- `CustomerNotFoundException`
- `InvalidLegalRepresentativeException`
- `UnauthorizedCustomerOperationException`

---

## 8.16 Output Ports

- `CustomerRepositoryPort`
- `AuthorizationPort`
- `OperationRepositoryPort`
- `AuditLogRepositoryPort`

---

## 8.17 Processing Flow

```text
BusinessCustomer
      │
      ▼
Validate Domain Model
      │
      ▼
Validate Legal Representative
      │
      ▼
Validate Legal Representative State
      │
      ▼
Check Business Identification Uniqueness
      │
      ▼
Check Required Persisted Relationships
      │
      ▼
Authorize Actor
      │
      ▼
Execute Domain Behavior
      │
      ▼
Persist BusinessCustomer
      │
      ▼
Register Operation
      │
      ▼
Register AuditLog
      │
      ▼
Return BusinessCustomer
```

---

# 9. Consult Customer

## 9.1 Description

Retrieves a customer and returns its corresponding Domain Model.

The service returns:

- `NaturalCustomer`
- `BusinessCustomer`

through their common `Customer` abstraction.

No persistence entity is exposed.

---

## 9.2 Actor

The actor must be authenticated and authorized.

---

## 9.3 Input Port

```java
interface ConsultCustomerUseCase {

    Customer consultCustomer(
        Customer customer
    );
}
```

The Input Port must operate on the Domain Model rather than defining the use case as a primitive-ID operation.

---

## 9.4 Input

```text
Customer
```

When the customer is not already fully loaded, the application boundary must construct the appropriate domain representation needed to identify the customer without violating the Domain Model parameter rule.

---

## 9.5 Preconditions

1. The supplied domain representation must be valid.
2. The customer must exist in persistence.
3. The actor must be authorized.

---

## 9.6 Existence

The service uses:

```text
CustomerRepositoryPort
```

to obtain the persisted customer.

If the customer does not exist:

```text
CustomerNotFoundException
```

---

## 9.7 Authorization

The authorization rules are:

| Actor | Permission |
|---|---|
| `TELLER_EMPLOYEE` | May consult any customer |
| `COMMERCIAL_EMPLOYEE` | May consult any customer without restriction |
| `INTERNAL_ANALYST` | May consult any customer |
| `NATURAL_CUSTOMER` | May consult only their own customer |
| `BUSINESS_OPERATOR` | May consult only the customer associated with `User.customer` |
| `BUSINESS_SUPERVISOR` | May consult only the customer associated with `User.customer` |

Authorization must be evaluated using Domain Models and Value Objects.

When actor/customer association requires persisted information, the service obtains it through the appropriate Output Port.

---

## 9.8 Domain Behavior

This is a read operation. No customer state is mutated.

The service retrieves and returns the domain representation.

---

## 9.9 Persistence

The service performs a read through:

```text
CustomerRepositoryPort
```

No persistence entity may leave the adapter layer.

---

## 9.10 Operation and Audit

Consultation is not a state-changing business operation.

Therefore:

- No `Operation` is required by default.
- No `AuditLog` is required by default.

If banking policy requires auditing customer consultations, the service must register an audit event through the designated audit mechanism without creating a false business mutation.

---

## 9.11 Output

```text
Customer
```

The concrete returned type is:

```text
NaturalCustomer
```

or:

```text
BusinessCustomer
```

---

## 9.12 Exceptions

Possible exceptions:

- `CustomerNotFoundException`
- `InvalidCustomerException`
- `UnauthorizedCustomerOperationException`

---

## 9.13 Output Ports

- `CustomerRepositoryPort`
- `AuthorizationPort`
- `AuditLogRepositoryPort` only when consultation auditing is required

---

## 9.14 Processing Flow

```text
Customer
      │
      ▼
Validate Domain Representation
      │
      ▼
Retrieve Customer
      │
      ▼
Validate Existence
      │
      ▼
Authorize Actor
      │
      ▼
Return Customer
```

If consultation auditing is required:

```text
Authorize
   │
   ▼
Retrieve Customer
   │
   ▼
Return Customer
   │
   └──► Register AuditLog
```

---

# 10. Update Customer

## 10.1 Description

Updates allowed information belonging to an existing customer.

The service receives the desired customer state as a Domain Model.

It must not receive isolated attributes.

---

## 10.2 Actor

The actor must be authorized to update the target customer.

---

## 10.3 Input Port

```java
interface UpdateCustomerUseCase {

    Customer updateCustomer(
        Customer customer
    );
}
```

---

## 10.4 Input

```text
Customer
```

The supplied object represents the desired state.

The service must compare or reconcile it with the persisted domain state where required.

---

## 10.5 Preconditions

1. Customer exists.
2. Supplied Domain Model is valid.
3. Only fields allowed by the domain may be changed.
4. Resulting customer state satisfies all domain invariants.
5. Relationships remain valid.
6. New identification is unique if identification changes are allowed.
7. Actor is authorized.

---

## 10.6 Load Existing Customer

The service retrieves the current persisted state through:

```text
CustomerRepositoryPort
```

The service must not assume that the supplied model alone is sufficient to determine whether an update is valid.

---

## 10.7 Authorization

Authorization must be evaluated against the actor and the existing/target customer.

Where applicable:

- Employees may update according to their assigned permissions.
- A customer may update only information belonging to themselves.
- Business users may update only their associated customer where permitted.

The exact permission matrix belongs to the Authorization domain.

---

## 10.8 Allowed Changes

The service must distinguish between:

```text
Mutable customer information
```

and:

```text
Identity / lifecycle attributes governed by another use case
```

Customer status must not be changed implicitly by `Update Customer`; status has its own dedicated use case.

Likewise, fields whose mutation has a dedicated business use case must not be silently changed through a generic update.

---

## 10.9 Domain Behavior

The service applies the permitted changes through domain behavior.

Conceptually:

```java
existingCustomer.updateFrom(
    customer
);
```

The exact method is determined by the Domain Model.

The service must not implement domain invariants through arbitrary field assignment.

---

## 10.10 Domain Validation

The resulting customer must satisfy:

- Valid identification, when applicable.
- Valid name.
- Valid email.
- Valid phone.
- Valid address.
- Valid customer relationships.
- Valid CustomerStatus.
- All Value Object constraints.
- All Customer domain invariants.

---

## 10.11 Identification Uniqueness

If identification changes are permitted by the domain, the service must verify uniqueness through:

```text
CustomerRepositoryPort
```

The check must exclude the customer currently being updated.

If another customer already owns the new identification:

```text
CustomerAlreadyExistsException
```

---

## 10.12 Persistence

Only after successful domain behavior:

```text
Updated Customer
      │
      ▼
CustomerRepositoryPort
```

---

## 10.13 Operation

A successful update is a business operation and must generate the corresponding `Operation` according to the operation policy.

No operation is recorded when validation, authorization, or persistence fails.

---

## 10.14 Audit

A successful update generates the corresponding `AuditLog` according to the audit policy.

The audit must represent the actual successful change.

---

## 10.15 Output

```text
Customer
```

The returned object represents the final persisted domain state.

---

## 10.16 Exceptions

Possible exceptions:

- `CustomerNotFoundException`
- `InvalidCustomerException`
- `CustomerAlreadyExistsException`
- `UnauthorizedCustomerOperationException`
- Domain-specific Value Object exceptions

---

## 10.17 Output Ports

- `CustomerRepositoryPort`
- `AuthorizationPort`
- `OperationRepositoryPort`
- `AuditLogRepositoryPort`

---

## 10.18 Processing Flow

```text
Customer (desired state)
      │
      ▼
Validate Domain Representation
      │
      ▼
Load Existing Customer
      │
      ▼
Validate Existence
      │
      ▼
Authorize Actor
      │
      ▼
Validate Allowed Changes
      │
      ▼
Validate External Rules
      │
      ▼
Execute Domain Update Behavior
      │
      ▼
Persist Updated Customer
      │
      ▼
Register Operation
      │
      ▼
Register AuditLog
      │
      ▼
Return Updated Customer
```

---

# 11. Change Customer Status

## 11.1 Description

Changes the `CustomerStatus` of an existing customer.

Customer status represents the state of the customer's banking relationship.

It is independent from `UserStatus`.

Changing a customer status must not automatically change the status of any related system user.

---

## 11.2 Actor

The actor must have explicit authorization to perform customer status changes.

---

## 11.3 Input Port

```java
interface ChangeCustomerStatusUseCase {

    Customer changeCustomerStatus(
        Customer customer
    );
}
```

The requested target status must be represented by the Domain Model or by a domain value/command object if such a type is defined by the Domain Model.

The service must not use:

```java
changeCustomerStatus(
    String customerId,
    CustomerStatus status
);
```

---

## 11.4 Preconditions

1. Customer exists.
2. Current CustomerStatus is valid.
3. Target CustomerStatus is valid.
4. Transition from current state to target state is allowed.
5. Actor is authorized.

---

## 11.5 Current State

The service must operate on the current persisted customer state.

If the supplied representation does not contain sufficient current state, the service retrieves it through:

```text
CustomerRepositoryPort
```

---

## 11.6 Status Transition

The domain determines whether a transition is valid.

Examples from the current Customer specification include:

```text
ACTIVE  → BLOCKED
ACTIVE  → INACTIVE
BLOCKED → ACTIVE
```

The definitive transition matrix belongs to the Customer Domain Model.

The database must never determine whether a transition is valid.

---

## 11.7 Domain Behavior

The service must invoke the domain behavior responsible for changing status.

Conceptually:

```java
customer.changeStatus(targetStatus);
```

The exact method and target-state representation must follow the Domain Model.

---

## 11.8 User Status Independence

The following concepts must remain independent:

```text
CustomerStatus
UserStatus
```

For example:

```text
CustomerStatus = BLOCKED
```

does not automatically mean:

```text
UserStatus = BLOCKED
```

Any user-status change requires its own business rule and use case.

---

## 11.9 Persistence

After successful domain behavior:

```text
Customer
      │
      ▼
CustomerRepositoryPort
```

---

## 11.10 Operation

A successful customer status change generates an `Operation`.

---

## 11.11 Audit

A successful status change generates an `AuditLog` containing the actual business action and relevant resulting state according to the Audit domain specification.

No audit must be registered for a failed transition.

---

## 11.12 Output

```text
Customer
```

The returned model contains the resulting CustomerStatus.

---

## 11.13 Exceptions

Possible exceptions:

- `CustomerNotFoundException`
- `InvalidCustomerException`
- `InvalidCustomerStatusException`
- `UnauthorizedCustomerOperationException`

---

## 11.14 Output Ports

- `CustomerRepositoryPort`
- `AuthorizationPort`
- `OperationRepositoryPort`
- `AuditLogRepositoryPort`

---

## 11.15 Processing Flow

```text
Customer
      │
      ▼
Load Current Customer
      │
      ▼
Validate Existence
      │
      ▼
Authorize Actor
      │
      ▼
Validate Current Status
      │
      ▼
Validate Status Transition
      │
      ▼
Execute changeStatus(...)
      │
      ▼
Persist Customer
      │
      ▼
Register Operation
      │
      ▼
Register AuditLog
      │
      ▼
Return Customer
```

---

# 12. Consult Customer Products

## 12.1 Description

Retrieves the banking products associated with a customer.

The current domain model identifies the following products/services for this consultation:

- `BankAccount`
- `Loan`
- `Transfer`

All returned objects must be Domain Models.

---

## 12.2 Actor

The actor must be authenticated and authorized to consult the customer's products.

The authorization must consider the relationship between the actor and the target customer.

---

## 12.3 Input Port

```java
interface ConsultCustomerProductsUseCase {

    CustomerProducts consultCustomerProducts(
        Customer customer
    );
}
```

`CustomerProducts` represents a conceptual aggregate/return structure containing the applicable Domain Models.

If the project Domain Model defines another return abstraction, that type must be used instead.

---

## 12.4 Input

```text
Customer
```

The service must not be defined only as:

```java
consultCustomerProducts(
    String customerId
);
```

when the identifier is being used as a substitute for the customer relationship.

---

## 12.5 Preconditions

1. Customer representation is valid.
2. Customer exists.
3. Actor is authorized.
4. Product associations are retrieved through the appropriate Output Ports.

---

## 12.6 Customer Existence

The service uses:

```text
CustomerRepositoryPort
```

when customer existence must be verified against persistence.

If the customer does not exist:

```text
CustomerNotFoundException
```

---

## 12.7 Authorization

The authorization rules must be evaluated before exposing product information.

For actors whose access depends on customer ownership or association, the service must validate the corresponding Domain Model relationship.

---

## 12.8 Product Retrieval

The service obtains the products through dedicated Output Ports:

```text
BankAccountRepositoryPort
LoanRepositoryPort
TransferRepositoryPort
```

Conceptually:

```text
                 Customer
                    │
                    ▼
        Consult Customer Products
                    │
          ┌─────────┼─────────┐
          │         │         │
          ▼         ▼         ▼
    BankAccount    Loan     Transfer
       Port         Port       Port
          │         │         │
          └─────────┼─────────┘
                    ▼
             Domain Models
```

---

## 12.9 Domain Relationships

Relationships returned to the Domain/Application layer must remain represented by Domain Models.

The service must not create business-layer structures such as:

```text
customerId
accountId
loanId
transferId
```

as substitutes for relationships.

Persistence adapters may use identifiers internally to locate records.

---

## 12.10 Empty Product Collections

A customer may legitimately have no products.

An empty result must not automatically be treated as an error.

Conceptually:

```text
Customer
   │
   ├── BankAccounts = []
   ├── Loans = []
   └── Transfers = []
```

The exact collection/return type belongs to the Domain/Application contract.

---

## 12.11 Domain Behavior

This is a read-only use case.

No Customer, BankAccount, Loan, or Transfer state is modified.

---

## 12.12 Operation and Audit

No business `Operation` is generated by default because this use case does not mutate a banking product.

If consultation auditing is required by the banking audit policy, an `AuditLog` may be registered through the appropriate audit mechanism.

---

## 12.13 Output

Conceptually:

```text
CustomerProducts
    ├── BankAccount[]
    ├── Loan[]
    └── Transfer[]
```

All returned elements are Domain Models.

---

## 12.14 Exceptions

Possible exceptions:

- `CustomerNotFoundException`
- `InvalidCustomerException`
- `UnauthorizedCustomerOperationException`

Product-specific retrieval exceptions may be propagated when defined by the corresponding subdomain.

---

## 12.15 Output Ports

- `CustomerRepositoryPort`
- `BankAccountRepositoryPort`
- `LoanRepositoryPort`
- `TransferRepositoryPort`
- `AuthorizationPort`
- `AuditLogRepositoryPort` only when consultation auditing is required

---

## 12.16 Processing Flow

```text
Customer
      │
      ▼
Validate Domain Representation
      │
      ▼
Validate Customer Existence
      │
      ▼
Authorize Actor
      │
      ▼
Retrieve Bank Accounts
      │
      ▼
Retrieve Loans
      │
      ▼
Retrieve Transfers
      │
      ▼
Build CustomerProducts
      │
      ▼
Return Domain Models
```

---

# 13. Input Ports

The Customer Management subdomain exposes the following Input Ports:

```text
RegisterNaturalCustomerUseCase
RegisterBusinessCustomerUseCase
ConsultCustomerUseCase
UpdateCustomerUseCase
ChangeCustomerStatusUseCase
ConsultCustomerProductsUseCase
```

Conceptually:

```java
interface RegisterNaturalCustomerUseCase {
    NaturalCustomer registerNaturalCustomer(
        NaturalCustomer customer
    );
}

interface RegisterBusinessCustomerUseCase {
    BusinessCustomer registerBusinessCustomer(
        BusinessCustomer customer
    );
}

interface ConsultCustomerUseCase {
    Customer consultCustomer(
        Customer customer
    );
}

interface UpdateCustomerUseCase {
    Customer updateCustomer(
        Customer customer
    );
}

interface ChangeCustomerStatusUseCase {
    Customer changeCustomerStatus(
        Customer customer
    );
}

interface ConsultCustomerProductsUseCase {
    CustomerProducts consultCustomerProducts(
        Customer customer
    );
}
```

Input Ports must not expose:

- REST DTOs.
- Persistence entities.
- Primitive IDs as business relationships.
- Infrastructure-specific types.

---

# 14. Output Ports

## 14.1 CustomerRepositoryPort

Responsible for customer persistence and retrieval.

Conceptual contract:

```java
interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Customer find(Customer customer);

    boolean exists(Customer customer);
}
```

The exact signatures may be refined by the global Output Port specification, but the port must remain domain-oriented.

### Responsibilities

- Persist Customer Domain Models.
- Retrieve Customer Domain Models.
- Verify customer existence.
- Support uniqueness validations when required.

### Must not expose

- `JpaRepository`
- `EntityManager`
- SQL
- persistence entities
- table names

---

## 14.2 BankAccountRepositoryPort

Responsible for retrieving `BankAccount` Domain Models associated with a customer.

Conceptually:

```java
interface BankAccountRepositoryPort {

    List<BankAccount> findByCustomer(
        Customer customer
    );
}
```

The exact method signature must follow the canonical Bank Account Output Port specification.

---

## 14.3 LoanRepositoryPort

Responsible for retrieving `Loan` Domain Models associated with a customer.

Conceptually:

```java
interface LoanRepositoryPort {

    List<Loan> findByCustomer(
        Customer customer
    );
}
```

The exact method signature must follow the canonical Loan Output Port specification.

---

## 14.4 TransferRepositoryPort

Responsible for retrieving `Transfer` Domain Models associated with a customer.

Conceptually:

```java
interface TransferRepositoryPort {

    List<Transfer> findByCustomer(
        Customer customer
    );
}
```

The exact method signature must follow the canonical Transfer Output Port specification.

---

## 14.5 AuthorizationPort

Responsible for obtaining authorization decisions when authorization information cannot be resolved exclusively from the Domain Models available to the service.

Conceptually:

```java
interface AuthorizationPort {

    boolean isAuthorized(
        Person actor,
        Customer customer,
        AuthorizationAction action
    );
}
```

The canonical Authorization domain specification determines the definitive signature and authorization model.

The port must not expose infrastructure authorization mechanisms.

---

## 14.6 OperationRepositoryPort

Responsible for registering successful business Operations.

Conceptually:

```java
interface OperationRepositoryPort {

    Operation save(Operation operation);
}
```

Only successful business actions must generate Operations.

---

## 14.7 AuditLogRepositoryPort

Responsible for registering audit records.

Conceptually:

```java
interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);
}
```

The exact audit structure belongs to the Audit domain.

Customer Services must not construct infrastructure-specific audit records.

---

# 15. Service-to-Port Matrix

| Service | Customer | Bank Account | Loan | Transfer | Authorization | Operation | Audit |
|---|---:|---:|---:|---:|---:|---:|---:|
| Register Natural Customer | ✓ | | | | ✓ | ✓ | ✓ |
| Register Business Customer | ✓ | | | | ✓ | ✓ | ✓ |
| Consult Customer | ✓ | | | | ✓ | | Optional |
| Update Customer | ✓ | | | | ✓ | ✓ | ✓ |
| Change Customer Status | ✓ | | | | ✓ | ✓ | ✓ |
| Consult Customer Products | ✓ | ✓ | ✓ | ✓ | ✓ | | Optional |

---

# 16. Authorization Matrix

The following matrix applies to customer consultation based on the current Customer specification.

| Actor | Consult Any Customer | Consult Own Customer |
|---|---:|---:|
| `TELLER_EMPLOYEE` | Yes | Yes |
| `COMMERCIAL_EMPLOYEE` | Yes | Yes |
| `INTERNAL_ANALYST` | Yes | Yes |
| `NATURAL_CUSTOMER` | No | Yes |
| `BUSINESS_OPERATOR` | No | Yes, associated customer only |
| `BUSINESS_SUPERVISOR` | No | Yes, associated customer only |

For mutation services, the Authorization domain is authoritative for the final permission matrix.

Customer Services must invoke authorization rather than duplicating role-permission rules throughout each service.

---

# 17. Operation and Audit Policy

## 17.1 Mutating services

The following successful operations are business actions:

- Register Natural Customer
- Register Business Customer
- Update Customer
- Change Customer Status

They must register:

```text
Operation
AuditLog
```

through the corresponding domain/output mechanisms.

---

## 17.2 Read-only services

The following services do not mutate the domain:

- Consult Customer
- Consult Customer Products

They do not create an `Operation` by default.

An `AuditLog` may still be created when banking security/audit policy requires recording sensitive consultations.

---

## 17.3 Failure rule

The sequence is transactional from the business perspective:

```text
Validation failure
      │
      └──► No Operation
           No AuditLog
```

```text
Authorization failure
      │
      └──► No Operation
           No AuditLog
```

```text
Domain behavior failure
      │
      └──► No Operation
           No AuditLog
```

The Operation and Audit records must represent an actual successful business action.

---

# 18. Exception Catalog

Customer Services may use the following domain exceptions:

```text
CustomerAlreadyExistsException
CustomerNotFoundException
InvalidCustomerException
InvalidCustomerStatusException
InvalidLegalRepresentativeException
UnauthorizedCustomerOperationException
```

Additional Value Object or Domain Model exceptions may be used where required.

Exceptions must represent business/domain failures rather than infrastructure implementation details.

---

# 19. Failure Behavior

Every service must fail before persistence when a mandatory validation fails.

## 19.1 Validation failure

```text
Input
  │
  ▼
Validation
  │
  └── FAIL → Domain Exception
```

No mutation is persisted.

---

## 19.2 Authorization failure

```text
Input
  │
  ▼
Authorization
  │
  └── FAIL → UnauthorizedCustomerOperationException
```

No mutation is persisted.

---

## 19.3 Persistence failure

If persistence fails, the service must not report the business operation as successfully completed.

Operation/Audit consistency must be handled by the application's transaction/consistency strategy.

Customer Services must not hide persistence failures or manufacture successful results.

---

# 20. Input Adapter Boundary

External representations are converted into Domain Models before entering the Customer Service.

```text
HTTP Request
     │
     ▼
Request DTO
     │
     ▼
Input Adapter / Mapper
     │
     ▼
Customer Domain Model
     │
     ▼
Input Port
     │
     ▼
Customer Service
```

DTOs are therefore allowed at the infrastructure/application boundary but must not enter the Domain Service contract.

---

# 21. Dependency Direction

The dependency direction must remain:

```text
                  Domain
                    │
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
 Customer Services      Output Ports
                              │
                              ▼
                       Output Adapters
                              │
                              ▼
                      External Resources
```

Customer Services depend on Output Port abstractions.

They must never depend directly on:

```text
MySQL
JPA
Spring Data
SQL
Repository Implementations
REST clients
```

---

# 22. Transactional Boundary

For mutating Customer Services, the application must guarantee that the business operation is not reported as successful before the required persistence and operation/audit steps satisfy the application's consistency policy.

Conceptually:

```text
Validate
   ↓
Authorize
   ↓
Domain Mutation
   ↓
Persist Customer
   ↓
Register Operation
   ↓
Register Audit
   ↓
Commit / Return
```

The exact transaction mechanism is an infrastructure concern and must not leak into the Domain Model.

---

# 23. Testing Requirements

Customer Services must be testable without requiring real infrastructure.

## 23.1 Domain tests

Test:

- Customer invariants.
- Natural customer age rule.
- Business customer legal representative rules.
- Customer status transitions.
- Customer status/User status independence.
- Value Object constraints.

---

## 23.2 Service tests

Each service must test at least:

### Successful execution

```text
Valid input
→ authorized
→ valid external state
→ domain behavior
→ persistence
→ operation/audit where applicable
→ expected output
```

### Validation failure

Verify that:

- Correct exception is raised.
- Customer is not persisted.
- No Operation is registered.
- No AuditLog is registered.

### Authorization failure

Verify that:

- Correct authorization exception is raised.
- No customer mutation occurs.
- No Operation is registered.
- No AuditLog is registered.

### External information failure

Verify that:

- The correct Output Port is invoked.
- The service does not access infrastructure directly.
- The business operation is not reported as successful.

---

# 24. Architectural Constraints

The following constraints are mandatory:

1. Business logic belongs to the Domain layer.
2. Customer Services operate on Domain Models and Value Objects.
3. Input Ports use Domain Models and Value Objects.
4. REST Request DTOs must not enter Customer Services.
5. Primitive identifiers must not replace Domain Model relationships.
6. Services must not receive isolated attributes that belong to a Domain Model.
7. Services must not access databases directly.
8. External information must be obtained through Output Ports.
9. Output Ports are abstractions owned by the appropriate inner layer.
10. Output Adapters implement Output Ports.
11. Persistence entities must never enter the Domain layer.
12. Controllers must not contain Customer business rules.
13. Domain validations must not be delegated to databases.
14. Domain relationships must remain represented by Domain Models.
15. CustomerStatus and UserStatus are independent concepts.
16. `BusinessCustomer.legalRepresentative` is a `NaturalCustomer` domain relationship.
17. Customer Services must not depend on Spring, JPA, SQL, HTTP, REST, or database implementations.
18. Significant successful customer mutations must register Operation and Audit according to the business/audit policy.
19. Failed business actions must not generate successful Operation/Audit records.
20. Customer Services must remain independently unit-testable.
21. Authorization must be explicit for every operation that requires permission.
22. Each service must clearly distinguish domain validation from external validation.
23. Each service must clearly identify its Output Ports.
24. Each service must define its successful output and relevant exceptions.
25. Customer status changes must occur through the dedicated status use case rather than an implicit generic update.
26. Empty product collections are valid results for customer product consultation.
27. Infrastructure identifiers may be used internally by adapters but must not become business-layer relationships.

---

# 25. Final Service Catalog

```text
Customer Management
│
├── Register Natural Customer
│   └── RegisterNaturalCustomerUseCase
│
├── Register Business Customer
│   └── RegisterBusinessCustomerUseCase
│
├── Consult Customer
│   └── ConsultCustomerUseCase
│
├── Update Customer
│   └── UpdateCustomerUseCase
│
├── Change Customer Status
│   └── ChangeCustomerStatusUseCase
│
└── Consult Customer Products
    └── ConsultCustomerProductsUseCase
```

---

# 26. Canonical Service Pattern Summary

All mutating Customer Services must follow this pattern:

```text
1. Validate Input Domain Model
2. Load Required Persistent State
3. Validate Existence
4. Validate Related Entities
5. Validate Ownership / Relationships
6. Authorize Actor
7. Execute Domain Behavior
8. Persist Domain Model
9. Register Operation
10. Register AuditLog
11. Return Domain Model
```

Read-only services follow:

```text
1. Validate Input Domain Model
2. Load Required Persistent State
3. Validate Existence
4. Authorize Actor
5. Retrieve Domain Models
6. Register AuditLog only when required
7. Return Domain Models
```

The Customer Services implementation must preserve these boundaries and must use the canonical Domain Models, Value Objects, Authorization model, Operation model, Audit model, and Output Port contracts defined by their respective specifications.
