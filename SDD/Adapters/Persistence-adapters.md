# Persistence Adapters Specification (Output Adapters)

## 1. Overview

Persistence Adapters reside in `application/adapters/persistence/`. They implement the **Output Ports** defined in `application/domain/ports/out/`.

To guarantee complete technology independence and prevent persistence entities/ORM leaks into the domain:
1. Persistence adapters do NOT use domain models directly as database entities/documents.
2. Every output adapter defines its own **Persistence Entities / Documents (Repository DTOs)**, **Mappers** (Domain Model ↔ Entity/Document), and **ORM Repositories**.
3. This SDD uses Java and Spring Data exclusively: Spring Data JPA for relational data and Spring Data MongoDB for audit documents.

---

## 2. Architecture & Data Flow

```text
Domain Service (domain/services)
      |
      v  Calls Output Port Interface
Output Port Interface (domain/ports/out)
      ^
      |  Implemented by Output Persistence Adapter
Persistence Adapter (adapters/persistence)
      |  1. Mapea Domain Model -> Repository Entity/Document
      |  2. Calls a Spring Data Repository
      v
Spring Data Repository (JPA / MongoDB)
      |
      v
Database (SQL / MongoDB)
```

---

## 3. Technology Stack Guidelines

### 3.1 Java Tech Stack Conventions
- **Relational Databases (SQL - MySQL / PostgreSQL):**
  - **Technology:** **Spring Data JPA** / Hibernate.
  - **Entities:** Annotate with `@Entity`, `@Table`, `@Id`, `@Column`.
  - **Repositories:** Extend `JpaRepository<Entity, ID>`.
- **NoSQL Databases (MongoDB - Audit Logs):**
  - **Technology:** **Spring Data MongoDB**.
  - **Documents:** Annotate with `@Document(collection = "...")`, `@Id`.
  - **Repositories:** Extend `MongoRepository<Document, ID>`.

---

## 4. Structure Pattern

```text
adapters/persistence/
├── jpa/                           <-- Relational Persistence
│   ├── entities/                 <-- Entity Repository DTOs
│   ├── mappers/                  <-- Domain <-> Entity Mappers
│   ├── repositories/             <-- Spring Data JPA Repositories
│   └── BankAccountJpaAdapter.java<-- Implements Output Port
│
└── mongodb/                       <-- NoSQL Audit Persistence
    ├── documents/                <-- Document Repository DTOs
    ├── mappers/                  <-- Domain <-> Document Mappers
    ├── repositories/             <-- Spring Data MongoDB Repositories
    └── AuditLogMongoAdapter.java <-- Implements Output Port
```

---

## 5. Code Pattern Example: Java Stack (Spring Data JPA)

### A. Repository Entity DTO (`BankAccountEntity.java`)
```java
package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class BankAccountEntity {

    @Id
    private String accountNumber;

    @Column(nullable = false)
    private String accountType;

    @Column(nullable = false)
    private String customerIdentification;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status;

    // Getters and Setters
}
```

### B. Entity Mapper (`BankAccountJpaMapper.java`)
```java
package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.BankAccountEntity;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;

public class BankAccountJpaMapper {

    public static BankAccountEntity toEntity(BankAccount domain) {
        if (domain == null) return null;
        BankAccountEntity entity = new BankAccountEntity();
        entity.setAccountNumber(domain.getAccountNumber());
        entity.setAccountType(domain.getAccountType().getCode());
        entity.setCustomerIdentification(domain.getCustomer() != null ? domain.getCustomer().getIdentification() : null);
        entity.setBalance(domain.getCurrentBalance());
        entity.setCurrency(domain.getCurrency().getCode());
        entity.setStatus(domain.getAccountStatus().getCode());
        return entity;
    }

    public static BankAccount toDomain(BankAccountEntity entity, Customer owner) {
        if (entity == null) return null;
        BankAccount domain = new BankAccount();
        domain.setIdentifier(entity.getAccountNumber());
        domain.setAccountType(AccountType.fromCode(entity.getAccountType()));
        domain.setOwner(owner);
        domain.setCurrentBalance(entity.getBalance());
        domain.setCurrency(Currency.fromCode(entity.getCurrency()));
        if (entity.getStatus() != null) {
            domain.setAccountStatus(AccountStatus.fromCode(entity.getStatus()));
        }
        return domain;
    }
}
```

### C. Spring Data JPA Repository (`SpringDataJpaBankAccountRepository.java`)
```java
package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataJpaBankAccountRepository extends JpaRepository<BankAccountEntity, String> {
    Optional<BankAccountEntity> findByAccountNumber(String accountNumber);
}
```

### D. Output Adapter Implementation (`BankAccountJpaAdapter.java`)
```java
package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.BankAccountEntity;
import application.adapters.persistence.jpa.mappers.BankAccountJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBankAccountRepository;
import application.domain.models.BankAccount;
import application.domain.ports.out.BankAccountRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BankAccountJpaAdapter implements BankAccountRepositoryPort {

    private final SpringDataJpaBankAccountRepository jpaRepository;

    public BankAccountJpaAdapter(SpringDataJpaBankAccountRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BankAccount save(BankAccount account) {
        BankAccountEntity entity = BankAccountJpaMapper.toEntity(account);
        BankAccountEntity saved = jpaRepository.save(entity);
        return BankAccountJpaMapper.toDomain(saved, account.getOwner());
    }

    @Override
    public Optional<BankAccount> findByIdentifier(BankAccount account) {
        return jpaRepository.findByAccountNumber(account.getIdentifier())
                .map(entity -> BankAccountJpaMapper.toDomain(entity, account.getOwner()));
    }
}
```

---

