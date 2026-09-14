# Software Architecture

## Overview

The Banking Information Management System follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The primary objective of this architecture is to isolate the business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology independence.

---

# Architectural Principles

The architecture is based on the following principles:

- Domain-first design.
- Separation of concerns.
- Dependency inversion.
- Technology independence.
- High cohesion.
- Low coupling.
- Explicit boundaries between layers.

The domain contains all business rules and never depends on external technologies.

---

# Architecture Layers

The application is organized into four major components:

```
Application
│
├── Adapters
│
├── Domain
│
└── Infrastructure
```

Each component has a clearly defined responsibility.

---

# Package Structure

```text
src/
└── main/
    └── java/ (or ts/)
        └── application/
            │
            ├── App.java
            │
            ├── adapters/
            │   │
            │   ├── rest/                          <-- REST Delivery Layer
            │   │   ├── controllers/               <-- REST Controllers by Role/Feature
            │   │   ├── dtos/
            │   │   │   ├── requests/              <-- Request DTOs
            │   │   │   └── responses/             <-- Response DTOs
            │   │   └── mappers/                   <-- DTO <-> Domain Mappers
            │   │
            │   ├── useCases/                      <-- Use Cases Implementation Layer
            │   │   ├── PublicAccessUseCaseImpl.java
            │   │   ├── NaturalCustomerUseCaseImpl.java
            │   │   ├── BusinessCustomerUseCaseImpl.java
            │   │   ├── BusinessOperatorUseCaseImpl.java
            │   │   ├── BusinessSupervisorUseCaseImpl.java
            │   │   ├── TellerEmployeeUseCaseImpl.java
            │   │   ├── CommercialEmployeeUseCaseImpl.java
            │   │   └── InternalAnalystUseCaseImpl.java
            │   │
            │   └── persistence/                   <-- Output Persistence Layer
            │       ├── jpa/ (or typeorm/)         <-- Relational Persistence (SQL)
            │       │   ├── entities/              <-- Repository Entities / DTOs
            │       │   ├── mappers/               <-- Entity <-> Domain Mappers
            │       │   ├── repositories/          <-- Spring Data JPA / TypeORM Repositories
            │       │   └── BankAccountJpaAdapter.java <-- Implements Output Port
            │       │
            │       └── mongodb/ (or mongoose/)    <-- NoSQL Persistence (Audit)
            │           ├── documents/             <-- Repository Documents / DTOs
            │           ├── mappers/               <-- Document <-> Domain Mappers
            │           ├── repositories/          <-- Spring Data Mongo / Mongoose Repositories
            │           └── AuditLogMongoAdapter.java <-- Implements Output Port
            │
            ├── domain/
            │   ├── models/                        <-- Pure Domain Entities
            │   ├── valueobjects/                  <-- Domain Value Objects
            │   ├── enums/                         <-- Domain Enumerations
            │   ├── services/                      <-- Domain Services (Pure Business Logic)
            │   ├── exceptions/                    <-- Domain Exceptions
            │   └── ports/
            │       ├── in/                        <-- Role Input Ports (Interfaces)
            │       │   ├── PublicAccessPort.java
            │       │   ├── NaturalCustomerPort.java
            │       │   ├── BusinessCustomerPort.java
            │       │   ├── BusinessOperatorPort.java
            │       │   ├── BusinessSupervisorPort.java
            │       │   ├── TellerEmployeePort.java
            │       │   ├── CommercialEmployeePort.java
            │       │   └── InternalAnalystPort.java
            │       └── out/                       <-- Output Ports (Interfaces)
            │           ├── CustomerRepositoryPort.java
            │           ├── UserRepositoryPort.java
            │           ├── BankAccountRepositoryPort.java
            │           ├── LoanRepositoryPort.java
            │           ├── TransferRepositoryPort.java
            │           └── AuditRepositoryPort.java
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/                      <-- JWT Provider & Security Filters
```

---

# Layer Responsibilities

## Application

The `application` package represents the root of the project.

It contains the application entry point and all architectural components.

### Responsibilities

- Application bootstrap.
- Component organization.
- Dependency composition.

---

## App.java

### Description

`App.java` is the application's entry point.

### Responsibilities

- Initialize the application.
- Load the infrastructure.
- Configure dependency injection.
- Start the REST server.

---

# Adapters

The adapters connect external technologies with the business domain.

Adapters translate external requests into domain operations and transform domain objects into technology-specific representations.

The domain never communicates directly with external systems.

---

## REST Adapters (`adapters/rest/`)

Expose HTTP REST endpoints to external clients and handle transport concerns.

### Responsibilities
- Receive HTTP requests and authenticate JWT tokens.
- Extract claims from JWT and reconstruct the `User` Domain Model.
- Evaluate the user's `SystemRole` against the requested Role Input Port.
- Convert `RequestDTO` into Domain Models using REST Mappers.
- Invoke the corresponding Role Input Port interface passing the reconstructed `User` domain model.
- Convert returned Domain Models into `ResponseDTO` for HTTP responses.

---

## Use Cases Adapters (`adapters/useCases/`)

Implement the **Role Input Ports** defined in `domain/ports/in/`.

### Responsibilities
- Provide concrete implementation for each Role Input Port (`PublicAccessUseCaseImpl`, `NaturalCustomerUseCaseImpl`, `InternalAnalystUseCaseImpl`, etc.).
- **Inject the concrete Domain Services** (`domain/services/*Service`) where business logic resides.
- Delegate use case execution to the injected Domain Services.

---

## Persistence Output Adapters (`adapters/persistence/`)

Connect Domain Output Ports (`domain/ports/out/`) with databases (relational SQL and NoSQL MongoDB).

Persistence terminology and ORM technologies are adapted according to the project language stack:

### Java Stack (Spring Data)
- **Relational Persistence (JPA / SQL):**
  - **Entities (`@Entity`):** Relational database mapping DTOs.
  - **Repositories:** Extend Spring Data `JpaRepository`.
  - **Mappers:** Bidirectional conversion (`Domain Model` ↔ `JPA Entity`).
  - **Adapters:** Implement Output Ports (e.g. `BankAccountJpaAdapter`).
- **NoSQL Persistence (MongoDB Audit):**
  - **Documents (`@Document`):** MongoDB collection mapping DTOs.
  - **Repositories:** Extend Spring Data `MongoRepository`.
  - **Mappers:** Bidirectional conversion (`Domain Model` ↔ `Mongo Document`).
  - **Adapters:** Implement Output Ports (e.g. `AuditLogMongoAdapter`).

### TypeScript Stack (TypeORM / Prisma / Mongoose)
- **Relational Persistence (SQL - TypeORM / Prisma):**
  - **Entities (`@Entity()` / Prisma Model):** Relational mapping DTOs.
  - **Repositories:** TypeORM Repositories / Custom Data Adapters.
  - **Mappers:** Bidirectional conversion (`Domain Model` ↔ `TypeORM Entity`).
  - **Adapters:** Implement Output Ports (e.g. `BankAccountTypeOrmAdapter`).
- **NoSQL Persistence (MongoDB - Mongoose):**
  - **Schemas / Documents:** Mongoose Schema definitions.
  - **Repositories:** Mongoose Models.
  - **Mappers:** Bidirectional conversion (`Domain Model` ↔ `Mongoose Document`).
  - **Adapters:** Implement Output Ports (e.g. `AuditLogMongoAdapter`).

---

# Domain

The Domain layer is the core of the application.

It contains all business rules and must remain independent from any external technology.

No class inside the domain may depend on:

- Spring
- JPA
- MongoDB
- HTTP
- REST
- JSON
- SQL

---

## Models

Contain the business entities.

Examples:

- Person
- Customer
- BankAccount
- Loan
- Transfer
- Operation
- AuditLog

These objects represent the banking business.

---

## Value Objects

Represent immutable business concepts.

Examples:

- SystemRole
- UserStatus
- LoanStatus
- Currency

Value Objects are compared by value instead of identity.

---

## Enums

Contain technical enumerations that do not require business behavior.

Examples:

- ApprovalDecision
- NotificationChannel
- AuditSeverity

---

## Services

Contain business logic that does not naturally belong to a single entity.

Examples:

- LoanApprovalService
- TransferApprovalService
- InterestCalculationService

Services coordinate business operations while preserving domain integrity.

---

## Ports

Ports define communication contracts between the domain and external technologies.

The domain owns all interfaces.

---

### Input Ports

Represent application use cases.

Examples:

- CreateAccountUseCase
- RequestLoanUseCase
- ApproveLoanUseCase
- CreateTransferUseCase

Input ports define what the system can do.

---

### Output Ports

Represent dependencies required by the domain.

Examples:

- AccountRepository
- LoanRepository
- AuditRepository
- NotificationService

Output ports define what the domain needs from external systems.

---

## Exceptions

Contains business exceptions.

Examples:

- InsufficientBalanceException
- LoanNotApprovedException
- InvalidTransferException

Business exceptions belong exclusively to the domain.

---

# Infrastructure

Infrastructure contains technical configuration required by the application.

It does not contain business logic.

---

## Config

Responsible for application configuration.

Examples:

- REST configuration
- Serialization
- Environment configuration

---

## Database

Contains database initialization and connection configuration.

Examples:

- MySQL configuration
- MongoDB configuration
- Connection pools

---

## Security

Contains authentication and authorization configuration.

Examples:

- JWT configuration
- Password encoder
- Authentication filters

---

# Dependency Flow

Dependencies always point toward the domain.

```
REST Controller
        │
        ▼
Input Port
        │
        ▼
Domain Service
        │
        ▼
Output Port
        │
        ▼
Persistence Adapter
        │
        ▼
Database
```

The domain never depends on adapters or infrastructure.

---

# Benefits

This architecture provides:

- Technology independence.
- High maintainability.
- Clear separation of concerns.
- Improved testability.
- Easier scalability.
- Better support for Domain-Driven Design.
- Easy replacement of frameworks or databases.
- Reusable business logic.
- Long-term maintainability.

---

# Architectural Constraints

The following rules must always be respected:

1. Business logic belongs exclusively to the Domain layer.
2. Controllers must not contain business rules.
3. DTOs must never enter the Domain layer.
4. Persistence entities must never be exposed through the API.
5. Communication between technologies and the Domain must occur only through Ports.
6. Adapters implement Ports but never define business rules.
7. Infrastructure depends on the Domain, never the opposite.
8. Every dependency must point toward the Domain.
9. Business entities must remain framework-independent.
10. The Domain must be fully testable without requiring infrastructure components.