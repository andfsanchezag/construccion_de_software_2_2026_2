# User Authentication Services

## Introduction

This document defines the services belonging to the **User Authentication** subdomain of the Banking Information Management System.

The services in this subdomain are responsible for:

- Registering users associated with customers.
- Registering internal employee users.
- Authenticating users.
- Validating user credentials.
- Managing user passwords.
- Managing user status.
- Consulting user information.

The services operate exclusively with **Domain Models** and **Value Objects**.

They must never depend directly on:

- Databases.
- Persistence entities.
- SQL.
- JPA.
- Spring.
- REST.
- HTTP.
- JSON.
- JWT libraries.
- Password hashing libraries.
- Infrastructure implementations.

Whenever information or functionality external to the Domain is required, the service must communicate through an **Output Port**.

---

# Domain Model Context

`User` is a Domain Model that inherits from `Person`.

The conceptual hierarchy is:

```text
Person
├── Customer
│   ├── NaturalCustomer
│   └── BusinessCustomer
│
└── User
````

Therefore, information that represents the identity of a person belongs to `Person`.

The `User` model represents the person's system identity and authentication information.

A user may be associated with a customer:

```text
User
 │
 └── customer : Customer
```

The relationship must be represented using the Domain Model itself.

It must not be represented as:

```text
relatedEntityId : String
```

or:

```text
customerId : String
```

when that identifier is being used as a substitute for the Domain Model relationship.

---

# Service Design Principles

## Domain Model Parameters

All services and Input Ports in this subdomain must receive **Domain Models or Value Objects** as parameters.

They must never receive:

* `String` identifiers.
* Primitive identifiers.
* Individual attributes belonging to a Domain Model.
* Request DTOs.
* Persistence entities.
* Database objects.

### Incorrect

```java
registerUser(
    String username,
    String password,
    String customerId
);
```

### Correct

```java
registerUser(User user);
```

The same principle applies to all services.

### Incorrect

```java
login(
    String username,
    String password
);
```

### Correct

```java
login(User user);
```

### Incorrect

```java
changeUserStatus(
    String userId,
    UserStatus status
);
```

### Correct

```java
changeUserStatus(User user);
```

The Domain Model must contain the information required by the service.

---

# External Information

A service must perform validations directly against the Domain Model whenever the required information is already available.

For example:

```text
User
├── username
├── password
├── role
├── status
└── customer
```

If additional information is required from outside the Domain Model, the service must use an Output Port.

For example:

```text
User
 │
 ▼
User Authentication Service
 │
 ▼
UserRepository
 │
 ▼
Persistence Adapter
 │
 ▼
Database
```

The service must never access the database directly.

---

# User Registration

The system supports two different user registration scenarios:

1. Registering a user associated with a customer.
2. Registering an internal employee user.

These operations have different authorization rules.

---

# 1. Register Customer User

## Description

Creates a `User` associated with an existing `Customer`.

The user may represent:

* A natural customer.
* A business customer.

The association is represented directly in the Domain Model:

```text
User
 │
 └── customer : Customer
```

The service must not receive the customer identifier separately.

---

## Input

```text
User
```

The `User` model must contain the information necessary to represent the new system user.

The associated customer must be represented as:

```text
Customer
```

and not:

```text
String customerId
```

---

## Domain Validations

### User Information

The service validates that the received `User` represents a valid domain state.

The validation may include:

* Valid username.
* Valid password.
* Valid role.
* Valid user status.
* Valid person information.
* Valid customer association.

The exact rules belong to the Domain Model and Value Objects.

---

### Customer Association

The user must have a valid customer association when registering a customer user.

If the required customer information is already contained in:

```text
User.customer
```

the service validates it directly.

If additional persisted information is required, the service must use:

```text
CustomerRepository
```

through an Output Port.

---

### Role Validation

The user's role must be compatible with the associated customer.

Conceptually:

```text
NaturalCustomer
       │
       ▼
NATURAL_CUSTOMER
```

and:

```text
BusinessCustomer
       │
       ▼
BUSINESS_CUSTOMER
```

The exact role compatibility rules belong to the Domain.

---

### Username Uniqueness

The username must be unique among system users.

This cannot be determined exclusively from the `User` Domain Model.

The service must use:

```text
UserRepository
```

through an Output Port.

Conceptually:

```text
User
 │
 └── username
       │
       ▼
UserRepository
       │
       ▼
Username already exists?
```

If the username already exists, registration must be rejected.

---

## Password Processing

The password must never be stored in plain text.

The service must use:

```text
PasswordSecurityPort
```

to process the password before persistence.

Conceptually:

```text
User
 │
 └── password
       │
       ▼
PasswordSecurityPort
       │
       ▼
Secure Password
       │
       ▼
UserRepository
```

The Domain must not depend on a concrete implementation such as BCrypt, Argon2, or Spring Security.

---

## Persistence

After all validations succeed, the service persists the `User` through:

```text
UserRepository
```

The service must never access the database directly.

---

# 2. Register Employee User

## Description

Creates a `User` representing an internal employee.

Employee user registration is different from customer user registration.

An employee user does not necessarily have an associated `Customer`.

The registration must be performed by an authorized **Internal Analyst**.

---

## Input

```text
User
```

The `User` model represents the employee.

The role must correspond to an internal employee role.

Examples include:

```text
TELLER_EMPLOYEE
COMMERCIAL_EMPLOYEE
BUSINESS_OPERATOR
BUSINESS_SUPERVISOR
INTERNAL_ANALYST
```

The exact available roles are defined by `SystemRole`.

---

## Registering User

The service must identify the user performing the registration.

The registering user must be an authorized Internal Analyst.

If the required information is already available in the Domain Model, it must be validated directly.

If the persisted state of the registering user is required, the service must use:

```text
UserRepository
```

through an Output Port.

Conceptually:

```text
Registering User
       │
       ▼
UserRepository
       │
       ▼
User
       │
       ▼
SystemRole
       │
       ▼
INTERNAL_ANALYST
       │
       ▼
Authorized
```

The controller must not implement this authorization rule.

---

## Employee Role Validation

The new user must have an employee role.

Customer roles must not be used for employee registration.

For example:

```text
NATURAL_CUSTOMER
BUSINESS_CUSTOMER
```

are not employee roles.

---

## Username Validation

The username must be unique.

The service must validate this through:

```text
UserRepository
```

---

## Password Processing

The password must be securely processed through:

```text
PasswordSecurityPort
```

The plain-text password must never be persisted.

---

## Persistence

After successful validation, the service persists the employee `User` through:

```text
UserRepository
```

---

# 3. Login

## Description

Authenticates a system `User` using the username and password supplied by the user.

The authentication process consists of:

1. Searching for the user in the database using the username.
2. Retrieving the persisted `User`.
3. Validating the supplied password against the stored password.
4. Validating that the `UserStatus` allows authentication.
5. Generating a JWT when authentication succeeds.
6. Returning the JWT to the caller.

The service does not directly access the database or JWT implementation.

---

## Input

The login operation receives a `User` Domain Model containing the authentication information.

Conceptually:

```text
User
├── username
└── password
```

The service must not receive:

```java
login(String username, String password);
```

It must receive:

```java
login(User user);
```

The Input Adapter converts the external request into the Domain Model before invoking the Input Port.

---

# Login Flow

```text
Login User
     │
     ▼
Login Input Port
     │
     ▼
User Authentication Service
     │
     ▼
UserRepository
     │
     │ Search using username
     ▼
Stored User
     │
     ▼
PasswordSecurityPort
     │
     │ Validate password
     ▼
Credentials Valid?
     │
 ┌───┴────┐
 │        │
 NO      YES
 │        │
 ▼        ▼
Reject   Validate UserStatus
          │
          ▼
       JwtTokenPort
          │
          ▼
          JWT
```

---

## Step 1: Search User

The service uses the `UserRepository` Output Port to retrieve the user corresponding to the supplied username.

Conceptually:

```text
User
 │
 └── username
       │
       ▼
UserRepository
       │
       ▼
Stored User
```

The service must not directly query:

* MySQL.
* SQL.
* JPA.
* Database repositories.

These concerns belong to the Output Adapter.

---

## Step 2: Validate Password

After retrieving the stored user, the service validates the supplied password against the stored password.

The validation is performed through:

```text
PasswordSecurityPort
```

Conceptually:

```text
Provided Password
       │
       ▼
PasswordSecurityPort
       ▲
       │
Stored Password
       │
       ▼
Credentials Valid?
```

The service must not implement password hashing or comparison itself.

---

## Step 3: Validate User Status

The service validates the `UserStatus` of the retrieved user.

For example:

```text
ACTIVE
```

may allow authentication.

While:

```text
INACTIVE
BLOCKED
```

may prevent authentication.

The exact status transition and authentication rules belong to the Domain.

`CustomerStatus` must not be used as a replacement for `UserStatus`.

---

## Step 4: Generate JWT

If:

* The user exists.
* The password is valid.
* The user status allows authentication.

the service requests JWT generation through:

```text
JwtTokenPort
```

Conceptually:

```text
Authenticated User
       │
       ▼
JwtTokenPort
       │
       ▼
JWT
```

The Domain must not depend directly on a JWT implementation.

The actual token generation, signing algorithm, secret keys, expiration, and JWT library belong to the adapter or infrastructure layer.

---

# JWT Claims

The authentication token represents the authenticated user.

The required user information for the token is:

```text
username
role
```

The password must **not** be included in the JWT.

Even though the password is part of the login request and is used to validate the credentials, it must never be exposed as a JWT claim.

The conceptual token is therefore:

```text
JWT
├── username
└── role
```

The password is used only for authentication:

```text
Provided Password
       │
       ▼
PasswordSecurityPort
       │
       ▼
Validation
       │
       ▼
Discarded
```

It must not become part of the authentication token.

---

## JWT Generation Responsibility

The Domain Service requests token generation through:

```text
JwtTokenPort
```

The concrete implementation is responsible for:

* Creating the JWT.
* Adding claims.
* Signing the token.
* Applying expiration.
* Applying JWT-specific configuration.

The Domain does not know these implementation details.

---

# Authentication Result

After successful authentication, the service returns an authentication result containing the generated JWT.

Conceptually:

```text
AuthenticationResult
    │
    └── token
```

The concrete representation of the JWT belongs to the authentication adapter/infrastructure.

---

# Failed Authentication

Authentication must fail when the required conditions are not satisfied.

---

## User Not Found

If the username does not correspond to an existing user, authentication must be rejected.

```text
Username
   │
   ▼
UserRepository
   │
   ▼
User Not Found
   │
   ▼
InvalidCredentialsException
```

The system should avoid exposing whether a username exists when returning authentication errors if the application security requirements require generic authentication failures.

---

## Invalid Password

If the password does not match the stored password:

```text
Provided Password
       │
       ▼
PasswordSecurityPort
       │
       ▼
Invalid
       │
       ▼
InvalidCredentialsException
```

---

## Invalid User Status

If the user exists but cannot authenticate because of its `UserStatus`:

```text
User
 │
 └── UserStatus
        │
        ▼
Authentication Allowed?
        │
        └── NO
             │
             ▼
     InvalidUserStatusException
```

---

# 4. Change User Password

## Description

Changes the password of an existing system user.

The operation must preserve the security rules defined by the Domain.

---

## Input

```text
User
```

The service must not receive:

```java
changePassword(
    String userId,
    String password
);
```

It must receive the appropriate Domain Model.

---

## Processing

```text
User
 │
 ▼
Validate User
 │
 ▼
Validate Authorization
 │
 ▼
Validate New Password
 │
 ▼
PasswordSecurityPort
 │
 ▼
Secure Password
 │
 ▼
UserRepository
```

---

## Validations

The service validates:

* User existence.
* User status.
* Authorization to change the password.
* Password business rules.

If external information is required, the service must use the appropriate Output Port.

---

## Password Security

The new password must be processed through:

```text
PasswordSecurityPort
```

The plain-text password must never be persisted.

---

# 5. Change User Status

## Description

Changes the `UserStatus` of a system user.

`UserStatus` represents the user's ability to access and operate within the system.

It is independent of:

```text
CustomerStatus
```

---

## Input

```text
User
```

The service must not receive:

```java
changeUserStatus(
    String userId,
    UserStatus status
);
```

It must receive the `User` Domain Model.

---

## Processing

```text
User
 │
 ▼
Validate Current UserStatus
 │
 ▼
Validate Status Transition
 │
 ▼
Change UserStatus
 │
 ▼
UserRepository
```

---

## Status Validation

The Domain determines whether the requested status is a valid state for the user.

Possible states include:

```text
ACTIVE
INACTIVE
BLOCKED
```

The exact allowed transitions belong to the Domain.

The database must not determine whether a status transition is valid.

---

## Authorization

The actor performing the status change must be authorized according to the business rules.

If the information required to determine authorization is not available in the Domain Model, the service must use an Output Port.

---

# 6. Consult User

## Description

Retrieves information associated with a system `User`.

The result must be represented as a Domain Model.

Persistence entities must never be returned by the service.

---

## Input

```text
User
```

The service must not use:

```java
consultUser(String userId);
```

as a substitute for the Domain Model.

---

## Processing

```text
User
 │
 ▼
User Authentication Service
 │
 ▼
UserRepository
 │
 ▼
User
```

The service may use additional Output Ports when the business operation requires external information.

---

# Output Ports

All User Authentication Services communicate with external resources exclusively through Output Ports.

The relevant ports are:

```text
UserRepository
CustomerRepository
PasswordSecurityPort
JwtTokenPort
```

---

# UserRepository

## Description

Defines the persistence operations required by User Authentication Services.

The port belongs to:

```text
domain/ports/out/
```

The implementation belongs to the persistence adapter.

---

## Responsibilities

Conceptual responsibilities include:

```text
Save User
Find User
Check User Existence
```

The port operates with Domain Models.

Conceptually:

```java
interface UserRepository {

    User save(User user);

    User find(User user);

    boolean exists(User user);
}
```

The exact interface can be refined in the dedicated Output Port documentation.

The persistence implementation may use the username to query the database, but this remains an implementation detail.

---

# CustomerRepository

## Description

Provides access to customer information when user operations require validation of the associated customer.

The relationship remains represented by:

```text
User
 │
 └── customer : Customer
```

The persistence adapter is responsible for translating the Domain relationship into the database representation.

---

# PasswordSecurityPort

## Description

Defines the contract required by the Domain to perform password security operations.

Conceptual responsibilities include:

```text
Secure Password
Validate Password
```

For example:

```java
interface PasswordSecurityPort {

    User secure(User user);

    boolean matches(User user);
}
```

The exact interface should be refined according to the Domain Model.

The implementation may use:

* BCrypt.
* Argon2.
* PBKDF2.
* Another secure hashing mechanism.

These technologies must not enter the Domain.

---

# JwtTokenPort

## Description

Defines the contract required by the Domain to generate the authentication JWT.

Conceptually:

```java
interface JwtTokenPort {

    AuthenticationToken generate(User user);
}
```

The implementation is responsible for:

* Creating the JWT.
* Adding the username claim.
* Adding the role claim.
* Signing the token.
* Applying expiration.
* Managing cryptographic configuration.

The Domain does not depend on the JWT implementation.

---

# Service and Port Interaction

The dependency direction must always remain:

```text
                 Domain
                   │
          ┌────────┴─────────┐
          │                  │
        Services        Output Ports
                             │
                             ▼
                     Output Adapters
                             │
             ┌───────────────┼───────────────┐
             ▼               ▼               ▼
          MySQL           Security       JWT Provider
```

The services depend on interfaces.

They never depend directly on implementations.

---

# Login Dependency Flow

```text
                 User
                  │
                  ▼
             Login Input Port
                  │
                  ▼
       User Authentication Service
                  │
        ┌─────────┼──────────┐
        │         │          │
        ▼         ▼          ▼
 UserRepository  Password   UserStatus
                 Security
                  │
                  ▼
            Credentials OK
                  │
                  ▼
             JwtTokenPort
                  │
                  ▼
                 JWT
```

---

# Customer User Registration Flow

```text
User
 │
 ├── customer : Customer
 │
 ├── username
 │
 ├── password
 │
 └── role
 │
 ▼
Register Customer User
 │
 ├── Validate Customer
 │
 ├── Validate Role
 │
 ├── Validate Username
 │
 ├── PasswordSecurityPort
 │
 └── UserRepository
```

---

# Employee User Registration Flow

```text
User
 │
 ├── employee information
 │
 ├── username
 │
 ├── password
 │
 └── employee role
 │
 ▼
Register Employee User
 │
 ├── Retrieve Registering User
 │
 ├── Validate INTERNAL_ANALYST
 │
 ├── Validate Employee Role
 │
 ├── Validate Username
 │
 ├── PasswordSecurityPort
 │
 └── UserRepository
```

---

# Input Ports

Input Ports represent the use cases exposed by this subdomain.

Conceptually:

```text
RegisterCustomerUserUseCase
RegisterEmployeeUserUseCase
LoginUseCase
ChangeUserPasswordUseCase
ChangeUserStatusUseCase
ConsultUserUseCase
```

Each Input Port must respect the Domain Model parameter rule.

---

## Example

```java
interface LoginUseCase {

    AuthenticationResult login(User user);
}
```

Another example:

```java
interface RegisterCustomerUserUseCase {

    User registerCustomerUser(User user);
}
```

The exact interface definitions should be documented separately in the Input Ports documentation.

---

# Input Adapter Flow

External representations must be converted into Domain Models before entering the Domain.

The flow is:

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
Domain Model
     │
     ▼
Input Port
     │
     ▼
Domain Service
```

DTOs must never enter the Domain.

---

# Validation Strategy

## Information Available in the Domain

If the information required for a validation already exists in the Domain Model, the service must validate it directly.

Examples:

```text
User.username
User.password
User.role
User.status
User.customer
Customer.status
```

An Output Port must not be called unnecessarily.

---

## Information Outside the Domain

If the validation requires information that cannot be determined from the Domain Model, the service must use an Output Port.

Examples:

```text
Existing username
Existing user
Existing customer
Stored password
JWT generation
```

Conceptually:

```text
Validation Required
        │
   ┌────┴────┐
   │         │
Domain     External
Data        Data
   │         │
   ▼         ▼
Domain    Output Port
Validation     │
               ▼
       External Resource
```

---

# User Status and Customer Status

The system maintains two independent status concepts.

```text
CustomerStatus
```

represents the operational state of the banking customer.

```text
UserStatus
```

represents the state of the system user's access.

For example:

```text
Customer
 │
 └── CustomerStatus = BLOCKED

User
 │
 └── UserStatus = ACTIVE
```

These states must not automatically modify each other unless an explicit business rule establishes such behavior.

A Customer Service must not automatically modify `UserStatus`.

A User Authentication Service must not automatically modify `CustomerStatus`.

---

# Authorization Rules

## Customer User Registration

Registration of a user associated with a customer can be performed according to the customer-user registration rules.

The operation does not require the registering actor to be an Internal Analyst unless another explicit business rule establishes that restriction.

---

## Employee User Registration

Registration of an employee user must be performed by an authorized:

```text
INTERNAL_ANALYST
```

The service must validate the role of the registering user using the Domain Model.

If the persisted state of the registering user is required, it must be retrieved through:

```text
UserRepository
```

---

# Exceptions

Conceptual Domain/Application exceptions for this subdomain include:

```text
UserAlreadyExistsException
UserNotFoundException
InvalidUserException
InvalidUserRoleException
InvalidUserStatusException
InvalidCustomerAssociationException
InvalidCredentialsException
UserNotAuthorizedException
PasswordValidationException
AuthenticationException
```

The exact exception catalog should be defined separately in the Domain Exceptions documentation.

---

# Operation and Audit

User-related actions that represent significant business events may generate an `Operation` and corresponding `AuditLog`.

Examples include:

* Customer user registration.
* Employee user registration.
* User status changes.
* Password changes.
* Successful authentication.
* Failed authentication when required by the business rules.

The User Authentication Services must not access MongoDB or another audit database directly.

If an operation must be recorded, the service must communicate with the appropriate Domain Service or Output Port.

Conceptually:

```text
User Authentication Service
             │
             ▼
      Business Operation
             │
       ┌─────┴─────┐
       ▼           ▼
   Operation     AuditLog
```

The detailed operation and audit behavior belongs to the **Operation and Audit** subdomain.

---

# Security Considerations

The following security rules are mandatory.

## Password

Passwords must never be:

* Stored in plaintext.
* Returned in API responses.
* Included in logs.
* Included in audit records.
* Included in JWT claims.

The password exists only as authentication input and as a securely processed representation.

---

## JWT

The JWT must contain the information necessary to identify and authorize the authenticated user.

The required conceptual claims are:

```text
username
role
```

The password must never be included.

JWT signing and validation remain infrastructure concerns.

---

# Complete Authentication Architecture

```text
                         REST API
                            │
                            ▼
                    Authentication
                       Controller
                            │
                            ▼
                      Login Request
                            │
                            ▼
                         Mapper
                            │
                            ▼
                           User
                            │
                            ▼
                     Login Input Port
                            │
                            ▼
                User Authentication Service
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
       UserRepository   PasswordSecurity  UserStatus
              │              │
              ▼              ▼
        Stored User    Password Valid?
              │
              └─────────────┬─────────────┘
                            │
                            ▼
                       JwtTokenPort
                            │
                            ▼
                           JWT
                            │
                            ▼
                    AuthenticationResult
                            │
                            ▼
                        REST Response
```

---

# Architectural Constraints

The following constraints are mandatory for all User Authentication Services:

1. Business logic belongs exclusively to the Domain layer.
2. `User` is a Domain Model.
3. `User` inherits from `Person`.
4. Services must receive Domain Models or Value Objects as parameters.
5. Services must never receive primitive identifiers as substitutes for Domain relationships.
6. A User-to-Customer relationship must be represented using `Customer`, not `String customerId`.
7. Services must never receive isolated attributes that represent part of a Domain Model.
8. Services must never receive REST Request DTOs.
9. Services must never receive persistence entities.
10. Services must never access databases directly.
11. External information must always be obtained through Output Ports.
12. Output Ports are interfaces owned by the Domain.
13. Output Adapters implement Output Ports.
14. User lookup during login is performed through `UserRepository`.
15. Login searches for the user using the username contained in the authentication Domain Model.
16. Password validation is performed through `PasswordSecurityPort`.
17. Passwords must never be stored in plaintext.
18. Passwords must never be included in JWT claims.
19. JWT generation is performed through `JwtTokenPort`.
20. The Domain must not depend on JWT libraries.
21. The Domain must not depend on Spring Security.
22. JWT signing and cryptographic configuration belong to infrastructure.
23. The JWT contains the authenticated username and role.
24. `UserStatus` and `CustomerStatus` are independent concepts.
25. Customer user registration and employee user registration are different use cases.
26. Employee user registration must be authorized by an Internal Analyst.
27. User status transitions must be validated by Domain rules.
28. Controllers must not implement authentication or authorization business rules.
29. DTOs must be converted into Domain Models before entering the Domain.
30. Persistence entities must never leave the persistence adapter.
31. Services must remain independent of MySQL, MongoDB, JPA, SQL, HTTP, REST, JSON, and security libraries.
32. All authentication-related business rules must remain testable without infrastructure.

```
```
