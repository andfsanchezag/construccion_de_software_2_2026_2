# Use Cases Adapters Specification

## 1. Overview

Use Case Adapters reside in `application/adapters/useCases/`. They serve as the concrete implementations of the **Role Input Ports** defined in `application/domain/ports/in/`.

Use Case Adapters bridge incoming application calls (from REST Controllers or other entry points) to the core **Domain Services** (`application/domain/services/`).

---

## 2. Architectural Role and Injections

### 2.1 Injections
Every Use Case Adapter:
1. Implements a specific Role Input Port interface (`domain/ports/in/*Port`).
2. **Injects the concrete Domain Service classes** (`domain/services/*Service`) that contain the actual business rules, domain validations, and Output Port connections.

```text
REST Controller (adapters/rest)
      |
      v  Calls Input Port Interface
Role Input Port (domain/ports/in)
      ^
      |  Implemented by
Use Case Adapter (adapters/useCases)
      |
      v  Injects & Delegates to
Domain Service (domain/services)
      |
      v  Injects & Calls Output Port
Output Port (domain/ports/out)
```

---

## 3. Structure and Naming Conventions

For every Role Input Port in `domain/ports/in/`, a corresponding implementation class exists in `adapters/useCases/`:

| Role Input Port (`domain/ports/in/`) | Use Case Implementation Class (`adapters/useCases/`) | Injected Domain Services (`domain/services/`) |
| :--- | :--- | :--- |
| `PublicAccessPort` | `PublicAccessUseCaseImpl` | `UserAuthenticationService`, `CustomerService` |
| `NaturalCustomerPort` | `NaturalCustomerUseCaseImpl` | `CustomerService`, `BankAccountService`, `LoanService`, `TransferService`, `OperationAuditService` |
| `BusinessCustomerPort` | `BusinessCustomerUseCaseImpl` | `CustomerService`, `BankAccountService`, `LoanService`, `TransferService`, `UserAuthenticationService` |
| `BusinessOperatorPort` | `BusinessOperatorUseCaseImpl` | `BankAccountService`, `TransferService`, `OperationAuditService` |
| `BusinessSupervisorPort` | `BusinessSupervisorUseCaseImpl` | `TransferService`, `OperationAuditService` |
| `TellerEmployeePort` | `TellerEmployeeUseCaseImpl` | `CustomerService`, `BankAccountService` |
| `CommercialEmployeePort` | `CommercialEmployeeUseCaseImpl` | `CustomerService`, `LoanService`, `BankAccountService` |
| `InternalAnalystPort` | `InternalAnalystUseCaseImpl` | `UserAuthenticationService`, `CustomerService`, `LoanService`, `OperationAuditService` |

---

## 4. Code Pattern Example (Java / Spring)

```java
package application.adapters.useCases;

import application.domain.models.*;
import application.domain.ports.in.InternalAnalystPort;
import application.domain.services.LoanService;
import application.domain.services.CustomerService;
import application.domain.services.OperationAuditService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternalAnalystUseCaseImpl implements InternalAnalystPort {

    private final LoanService loanService;
    private final CustomerService customerService;
    private final OperationAuditService operationAuditService;

    public InternalAnalystUseCaseImpl(LoanService loanService,
                                      CustomerService customerService,
                                      OperationAuditService operationAuditService) {
        this.loanService = loanService;
        this.customerService = customerService;
        this.operationAuditService = operationAuditService;
    }

    @Override
    public Loan approveLoan(User user, Loan loan) {
        // Delegates execution directly to the Domain Service containing business rules and output port calls
        return loanService.approveLoan(user, loan);
    }

    @Override
    public Loan disburseLoan(User user, Loan loan, BankAccount destinationAccount) {
        return loanService.disburseLoan(user, loan, destinationAccount);
    }

    @Override
    public List<AuditLog> consultAuditLog(User user) {
        return operationAuditService.consultAuditLog(user);
    }
}
```

---

## 5. Code Pattern Example (TypeScript / NestJS / Express)

```typescript
import { Injectable } from '@nestjs/common';
import { InternalAnalystPort } from '../../domain/ports/in/InternalAnalystPort';
import { LoanService } from '../../domain/services/LoanService';
import { User, Loan, BankAccount, AuditLog } from '../../domain/models';

@Injectable()
export class InternalAnalystUseCaseImpl implements InternalAnalystPort {
  constructor(
    private readonly loanService: LoanService,
    private readonly operationAuditService: OperationAuditService,
  ) {}

  async approveLoan(user: User, loan: Loan): Promise<Loan> {
    return this.loanService.approveLoan(user, loan);
  }

  async disburseLoan(user: User, loan: Loan, destinationAccount: BankAccount): Promise<Loan> {
    return this.loanService.disburseLoan(user, loan, destinationAccount);
  }

  async consultAuditLog(user: User): Promise<AuditLog[]> {
    return this.operationAuditService.consultAuditLog(user);
  }
}
```
