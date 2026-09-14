# Persistence Adapters Specification (Output Adapters)

## 1. Overview

Persistence Adapters reside in `application/adapters/persistence/`. They implement the **Output Ports** defined in `application/domain/ports/out/`.

To guarantee complete technology independence and prevent persistence entities/ORM leaks into the domain:
1. Persistence adapters do NOT use domain models directly as database entities/documents.
2. Every output adapter defines its own **Persistence Entities / Documents (Repository DTOs)**, **Mappers** (Domain Model ↔ Entity/Document), and **ORM Repositories**.
3. Technology and ORM naming conventions are adapted explicitly according to the project language stack: **Java (Spring Data)** or **TypeScript (TypeORM / Mongoose / Prisma)**.

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
      |  2. Calls Spring Data Repository / TypeORM / Mongoose
      v
ORM Repository (Spring Data JPA / Mongo / TypeORM / Mongoose)
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

### 3.2 TypeScript Tech Stack Conventions
- **Relational Databases (SQL - PostgreSQL / MySQL):**
  - **Technology:** **TypeORM** or **Prisma**.
  - **Entities:** Decorate with `@Entity()`, `@PrimaryGeneratedColumn()`, `@Column()`.
  - **Repositories:** Use TypeORM `@InjectRepository()` / Custom Repository.
- **NoSQL Databases (MongoDB - Audit Logs):**
  - **Technology:** **Mongoose** / **Prisma Mongo**.
  - **Schemas/Documents:** Define `Schema` with Mongoose `@Prop()`, `@Schema()`.
  - **Repositories:** Use Mongoose `InjectModel()`.

---

## 4. Structure Pattern

```text
adapters/persistence/
├── jpa/ (or typeorm/)            <-- Relational Persistence
│   ├── entities/                 <-- Entity Repository DTOs
│   ├── mappers/                  <-- Domain <-> Entity Mappers
│   ├── repositories/             <-- Spring Data JPA / TypeORM Repositories
│   └── BankAccountJpaAdapter.java<-- Implements Output Port
│
└── mongodb/ (or mongoose/)       <-- NoSQL Audit Persistence
    ├── documents/                <-- Document Repository DTOs
    ├── mappers/                  <-- Domain <-> Document Mappers
    ├── repositories/             <-- Spring Data Mongo / Mongoose Repositories
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
import application.domain.enums.AccountStatus;

public class BankAccountJpaMapper {

    public static BankAccountEntity toEntity(BankAccount domain) {
        if (domain == null) return null;
        BankAccountEntity entity = new BankAccountEntity();
        entity.setAccountNumber(domain.getAccountNumber());
        entity.setAccountType(domain.getAccountType());
        entity.setCustomerIdentification(domain.getCustomer() != null ? domain.getCustomer().getIdentification() : null);
        entity.setBalance(domain.getAvailableBalance());
        entity.setCurrency(domain.getCurrency());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        return entity;
    }

    public static BankAccount toDomain(BankAccountEntity entity) {
        if (entity == null) return null;
        BankAccount domain = new BankAccount();
        domain.setAccountNumber(entity.getAccountNumber());
        domain.setAccountType(entity.getAccountType());
        domain.setAvailableBalance(entity.getBalance());
        domain.setCurrency(entity.getCurrency());
        if (entity.getStatus() != null) {
            domain.setStatus(AccountStatus.valueOf(entity.getStatus()));
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
        return BankAccountJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<BankAccount> findByAccountNumber(BankAccount account) {
        return jpaRepository.findByAccountNumber(account.getAccountNumber())
                .map(BankAccountJpaMapper::toDomain);
    }
}
```

---

## 6. Code Pattern Example: TypeScript Stack (TypeORM & Mongoose)

### A. TypeORM Entity DTO (`BankAccountEntity.ts`)
```typescript
import { Entity, PrimaryColumn, Column } from 'typeorm';

@Entity('bank_accounts')
export class BankAccountEntity {
  @PrimaryColumn()
  accountNumber: string;

  @Column()
  accountType: string;

  @Column()
  customerIdentification: string;

  @Column('numeric')
  balance: number;

  @Column()
  currency: string;

  @Column()
  status: string;
}
```

### B. TypeORM Adapter Implementation (`BankAccountTypeOrmAdapter.ts`)
```typescript
import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BankAccountRepositoryPort } from '../../../domain/ports/out/BankAccountRepositoryPort';
import { BankAccount } from '../../../domain/models/BankAccount';
import { BankAccountEntity } from './entities/BankAccountEntity';
import { BankAccountTypeOrmMapper } from './mappers/BankAccountTypeOrmMapper';

@Injectable()
export class BankAccountTypeOrmAdapter implements BankAccountRepositoryPort {
  constructor(
    @InjectRepository(BankAccountEntity)
    private readonly repository: Repository<BankAccountEntity>,
  ) {}

  async save(account: BankAccount): Promise<BankAccount> {
    const entity = BankAccountTypeOrmMapper.toEntity(account);
    const saved = await this.repository.save(entity);
    return BankAccountTypeOrmMapper.toDomain(saved);
  }

  async findByAccountNumber(account: BankAccount): Promise<BankAccount | null> {
    const entity = await this.repository.findOne({ where: { accountNumber: account.accountNumber } });
    return entity ? BankAccountTypeOrmMapper.toDomain(entity) : null;
  }
}
```

### C. Mongoose Document DTO & Adapter for MongoDB Audit Log (`AuditLogMongoAdapter.ts`)
```typescript
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { AuditRepositoryPort } from '../../../domain/ports/out/AuditRepositoryPort';
import { AuditLog } from '../../../domain/models/AuditLog';
import { AuditLogDocument } from './documents/AuditLogDocument';
import { AuditLogMongoMapper } from './mappers/AuditLogMongoMapper';

@Injectable()
export class AuditLogMongoAdapter implements AuditRepositoryPort {
  constructor(
    @InjectModel(AuditLogDocument.name)
    private readonly auditModel: Model<AuditLogDocument>,
  ) {}

  async registerAuditEvent(auditLog: AuditLog): Promise<AuditLog> {
    const docData = AuditLogMongoMapper.toDocument(auditLog);
    const createdDoc = new this.auditModel(docData);
    const saved = await createdDoc.save();
    return AuditLogMongoMapper.toDomain(saved);
  }
}
```
