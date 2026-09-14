# Input Ports (Role-Based Use Case Interfaces)

## 1. Overview

Input Ports define the application entry contracts through which external delivery mechanisms (such as REST Controllers) interact with the core domain.

In this architecture, **Input Ports are organized strictly by System Roles (`SystemRole`)**, plus a public access port for unauthenticated operations. This ensures that:
- Every role exposes a dedicated, cohesive interface with the exact operations allowed for that role.
- REST Controllers and Security filters evaluate role permissions directly against the targeted Role Input Port.
- Methods in Input Ports operate exclusively on **Domain Models** and **Value Objects**, receiving the `User` domain object (reconstructed from JWT claims) and domain entities as parameters.

---

# 2. General Principles

### 2.1 Role Isolation
Each system role interacts with the application through its corresponding Role Input Port interface in `domain/ports/in/`:
- `PublicAccessPort` (Public / Unauthenticated)
- `NaturalCustomerPort` (`NATURAL_CUSTOMER`)
- `BusinessCustomerPort` (`BUSINESS_CUSTOMER`)
- `BusinessOperatorPort` (`BUSINESS_OPERATOR`)
- `BusinessSupervisorPort` (`BUSINESS_SUPERVISOR`)
- `TellerEmployeePort` (`TELLER_EMPLOYEE`)
- `CommercialEmployeePort` (`COMMERCIAL_EMPLOYEE`)
- `InternalAnalystPort` (`INTERNAL_ANALYST`)

### 2.2 Domain Model Parameters
All Input Port methods receive **Domain Models** (e.g., `User`, `BankAccount`, `Loan`, `Transfer`, `Customer`) instead of primitive IDs or DTOs.
The `User` domain model passed to each service contains the identity, role, and associated customer details reconstructed from the JWT token claims.

---

# 3. Input Port Definitions

## 3.1 PublicAccessPort
Exposes public operations that do not require prior authentication or are required for initial onboarding and authentication.

```java
public interface PublicAccessPort {
    User login(User user);
    void logout(User user);
    NaturalCustomer registerNaturalCustomer(NaturalCustomer customer);
    BusinessCustomer registerBusinessCustomer(BusinessCustomer customer);
    User registerCustomerUser(User user);
}
```

---

## 3.2 NaturalCustomerPort (`NATURAL_CUSTOMER`)
Exposes operations for individual natural person customers over their own accounts, loans, and transfers.

```java
public interface NaturalCustomerPort {
    Customer consultMyProfile(User user);
    Customer updateMyProfile(User user, Customer customer);
    CustomerProducts consultMyProducts(User user);
    
    // Bank Accounts
    List<BankAccount> consultMyAccounts(User user);
    BigDecimal consultAccountBalance(User user, BankAccount account);
    
    // Loans
    Loan requestLoan(User user, Loan loan);
    Loan consultLoan(User user, Loan loan);
    Loan registerLoanPayment(User user, Loan loan, BigDecimal amount);
    
    // Transfers & Operations
    Transfer createTransfer(User user, Transfer transfer);
    Transfer executeTransfer(User user, Transfer transfer);
    List<Operation> consultMyOperations(User user);
}
```

---

## 3.3 BusinessCustomerPort (`BUSINESS_CUSTOMER`)
Exposes corporate management operations for legal entity representatives.

```java
public interface BusinessCustomerPort {
    BusinessCustomer consultCompanyProfile(User user);
    CustomerProducts consultCompanyProducts(User user);
    User registerCompanyUser(User user, User newCompanyUser);
    
    List<BankAccount> consultCompanyAccounts(User user);
    Loan requestCompanyLoan(User user, Loan loan);
    
    Transfer approveCompanyTransfer(User user, Transfer transfer);
    Transfer rejectCompanyTransfer(User user, Transfer transfer);
}
```

---

## 3.4 BusinessOperatorPort (`BUSINESS_OPERATOR`)
Exposes operational transaction capabilities delegated by a business customer.

```java
public interface BusinessOperatorPort {
    List<BankAccount> consultCompanyAccounts(User user);
    Transfer createCompanyTransfer(User user, Transfer transfer);
    Transfer submitTransferForApproval(User user, Transfer transfer);
    List<Operation> consultCompanyOperations(User user);
}
```

---

## 3.5 BusinessSupervisorPort (`BUSINESS_SUPERVISOR`)
Exposes approval and oversight capabilities for company operations.

```java
public interface BusinessSupervisorPort {
    List<Transfer> consultPendingTransfers(User user);
    Transfer approveTransfer(User user, Transfer transfer);
    Transfer rejectTransfer(User user, Transfer transfer);
    List<Operation> consultCompanyOperations(User user);
}
```

---

## 3.6 TellerEmployeePort (`TELLER_EMPLOYEE`)
Exposes teller and service desk operations for over-the-counter transactions.

```java
public interface TellerEmployeePort {
    Customer consultCustomer(User user, Customer customer);
    BankAccount openBankAccount(User user, BankAccount account);
    BankAccount consultBankAccount(User user, BankAccount account);
    BigDecimal consultAccountBalance(User user, BankAccount account);
    
    BankAccount depositFunds(User user, BankAccount account, BigDecimal amount);
    BankAccount withdrawFunds(User user, BankAccount account, BigDecimal amount);
    
    BankAccount blockBankAccount(User user, BankAccount account);
    BankAccount unblockBankAccount(User user, BankAccount account);
    BankAccount closeBankAccount(User user, BankAccount account);
}
```

---

## 3.7 CommercialEmployeePort (`COMMERCIAL_EMPLOYEE`)
Exposes commercial and account executive operations for customer onboarding and product advisory.

```java
public interface CommercialEmployeePort {
    Customer consultCustomer(User user, Customer customer);
    Customer updateCustomer(User user, Customer customer);
    CustomerProducts consultCustomerProducts(User user, Customer customer);
    
    Loan requestLoanOnBehalfOfCustomer(User user, Customer customer, Loan loan);
    Loan consultLoanStatus(User user, Loan loan);
    BankAccount openBankAccount(User user, BankAccount account);
}
```

---

## 3.8 InternalAnalystPort (`INTERNAL_ANALYST`)
Exposes high-privilege operations for risk analysis, loan approval/disbursement, internal user management, and audit log inspection.

```java
public interface InternalAnalystPort {
    User registerEmployeeUser(User user, User newEmployee);
    Customer changeCustomerStatus(User user, Customer customer, CustomerStatus newStatus);
    User changeUserStatus(User user, User targetUser, UserStatus newStatus);
    
    Loan approveLoan(User user, Loan loan);
    Loan rejectLoan(User user, Loan loan);
    Loan disburseLoan(User user, Loan loan, BankAccount destinationAccount);
    Loan closeLoan(User user, Loan loan);
    
    List<AuditLog> consultAuditLog(User user);
    List<Operation> consultAllOperations(User user);
}
```
