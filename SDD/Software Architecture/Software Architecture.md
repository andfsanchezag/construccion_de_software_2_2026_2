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
    └── java/
        └── application/
            │
            ├── App.java
            │
            ├── adapters/
            │   │
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       └── mappers/
            │   │
            │   └── out/
            │       └── persistence/
            │           ├── mysql/
            │           │   ├── adapters/
            │           │   ├── entities/
            │           │   ├── repositories/
            │           │   └── mappers/
            │           │
            │           └── mongodb/
            │               ├── adapters/
            │               ├── documents/
            │               ├── repositories/
            │               └── mappers/
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   ├── exceptions/
            │   └── ports/
            │       ├── in/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/
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

## Input Adapters

Input adapters expose the application to external clients.

Current implementation:

```
adapters/in/rest
```

### Responsibilities

- Receive HTTP requests.
- Validate incoming data.
- Convert Request DTOs into Domain Models.
- Execute application use cases.
- Convert domain results into Response DTOs.

---

### Controllers

Controllers expose REST endpoints.

Responsibilities:

- Receive HTTP requests.
- Delegate execution to the domain.
- Return HTTP responses.

Controllers must never implement business rules.

---

### Requests

Request DTOs represent incoming HTTP payloads.

Responsibilities:

- Receive client data.
- Validate input.
- Transport data into the application.

These objects must not contain business logic.

---

### Responses

Response DTOs represent outgoing HTTP responses.

Responsibilities:

- Return processed information.
- Hide internal domain implementation.
- Standardize API responses.

---

### Mappers

Responsible for converting between:

- Request DTO ↔ Domain Model
- Domain Model ↔ Response DTO

This prevents the domain from depending on transport objects.

---

# Output Adapters

Output adapters connect the domain with external resources.

Examples:

- Databases
- Notification services
- External APIs
- Messaging systems

Current implementation:

```
Persistence
├── MySQL
└── MongoDB
```

---

## MySQL Adapter

Responsible for relational persistence.

### Components

#### Entities

Represent relational database tables.

#### Repositories

Implement persistence operations.

#### Mappers

Convert Domain Models into database entities.

#### Adapters

Implement Domain Output Ports.

---

## MongoDB Adapter

Responsible for storing audit information.

### Components

#### Documents

Represent MongoDB collections.

#### Repositories

Provide document persistence.

#### Mappers

Convert domain objects into MongoDB documents.

#### Adapters

Implement audit persistence ports.

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