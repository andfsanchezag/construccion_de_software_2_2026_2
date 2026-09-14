# API REST Endpoints Specification & Contracts

## 1. Overview

This document specifies the exact REST API endpoints, HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`), required HTTP Headers, Query Parameters, Request Body DTOs, and Response DTOs for every operation supported by the system.

The endpoints are organized by **System Roles** corresponding to the **Role Input Ports** (`domain/ports/in/`).

---

## 2. Global Headers & Security Conventions

### Standard Headers
For all protected endpoints (all endpoints except Public Access Login & Registration):
- `Authorization`: `Bearer <jwt_token>` (Required)
- `Content-Type`: `application/json` (Required for `POST`, `PUT`, `PATCH`)
- `Accept`: `application/json` (Required)

---

## 3. Public Access Endpoints (`PublicAccessPort`)

### 3.1. Authentication (Login)
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/login`
- **Headers:** `Content-Type: application/json`
- **Request Body (`LoginRequestDTO`):**
```json
{
  "username": "user123",
  "password": "SecurePassword123!"
}
```
- **Response (`LoginResponseDTO` - HTTP 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": "usr_99812",
    "username": "user123",
    "email": "user@example.com",
    "role": "NATURAL_CUSTOMER"
  }
}
```

### 3.2. Logout
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/logout`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Request Body:** None
- **Response (HTTP 204 No Content)**

### 3.3. Self-Register Natural Customer
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/register/natural-customer`
- **Headers:** `Content-Type: application/json`
- **Request Body (`RegisterNaturalCustomerRequestDTO`):**
```json
{
  "identification": "1017123456",
  "name": "Juan Perez",
  "email": "juan.perez@example.com",
  "phoneNumber": "3001234567",
  "address": "Calle 50 # 40 - 20",
  "birthDate": "1995-08-15"
}
```
- **Response (`CustomerResponseDTO` - HTTP 201 Created):**
```json
{
  "identification": "1017123456",
  "name": "Juan Perez",
  "email": "juan.perez@example.com",
  "status": "ACTIVE",
  "customerType": "NATURAL"
}
```

### 3.4. Self-Register Business Customer
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/register/business-customer`
- **Headers:** `Content-Type: application/json`
- **Request Body (`RegisterBusinessCustomerRequestDTO`):**
```json
{
  "identification": "900123456-1",
  "name": "Empresa Tech S.A.S.",
  "email": "contacto@techsas.com",
  "phoneNumber": "6041234567",
  "address": "Carrera 43A # 1 - 50",
  "legalRepresentativeIdentification": "1017123456"
}
```
- **Response (`BusinessCustomerResponseDTO` - HTTP 201 Created):**
```json
{
  "identification": "900123456-1",
  "name": "Empresa Tech S.A.S.",
  "email": "contacto@techsas.com",
  "status": "ACTIVE",
  "customerType": "BUSINESS",
  "legalRepresentative": {
    "identification": "1017123456",
    "name": "Juan Perez"
  }
}
```

### 3.5. Register Customer User (Account Creation)
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/register/user`
- **Headers:** `Content-Type: application/json`
- **Request Body (`RegisterUserRequestDTO`):**
```json
{
  "customerIdentification": "1017123456",
  "username": "juanperez95",
  "password": "StrongPassword123!",
  "role": "NATURAL_CUSTOMER"
}
```
- **Response (`UserResponseDTO` - HTTP 201 Created):**
```json
{
  "userId": "usr_1001",
  "username": "juanperez95",
  "role": "NATURAL_CUSTOMER",
  "status": "ACTIVE"
}
```

---

## 4. Natural Customer Endpoints (`NaturalCustomerPort`)

### 4.1. Consult My Profile
- **HTTP Method:** `GET`
- **Path:** `/api/v1/natural-customer/profile`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`CustomerResponseDTO` - HTTP 200 OK)**

### 4.2. Update My Profile
- **HTTP Method:** `PUT`
- **Path:** `/api/v1/natural-customer/profile`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`UpdateCustomerProfileRequestDTO`):**
```json
{
  "email": "juan.perez.new@example.com",
  "phoneNumber": "3009876543",
  "address": "Avenida El Poblado # 10 - 12"
}
```
- **Response (`CustomerResponseDTO` - HTTP 200 OK)**

### 4.3. Consult My Accounts
- **HTTP Method:** `GET`
- **Path:** `/api/v1/natural-customer/accounts`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`List<BankAccountResponseDTO>` - HTTP 200 OK)**

### 4.4. Consult Account Balance
- **HTTP Method:** `GET`
- **Path:** `/api/v1/natural-customer/accounts/{accountNumber}/balance`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`AccountBalanceResponseDTO` - HTTP 200 OK):**
```json
{
  "accountNumber": "CTA-100200300",
  "availableBalance": 2500000.50,
  "currency": "COP"
}
```

### 4.5. Request Loan
- **HTTP Method:** `POST`
- **Path:** `/api/v1/natural-customer/loans`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`RequestLoanRequestDTO`):**
```json
{
  "loanType": "PERSONAL",
  "requestedAmount": 5000000.00,
  "termInMonths": 24,
  "destinationAccountNumber": "CTA-100200300"
}
```
- **Response (`LoanResponseDTO` - HTTP 201 Created):**
```json
{
  "loanId": "LOAN-5501",
  "loanType": "PERSONAL",
  "requestedAmount": 5000000.00,
  "status": "UNDER_REVIEW",
  "termInMonths": 24
}
```

### 4.6. Consult My Loan Details
- **HTTP Method:** `GET`
- **Path:** `/api/v1/natural-customer/loans/{loanId}`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`LoanResponseDTO` - HTTP 200 OK)**

### 4.7. Register Loan Payment
- **HTTP Method:** `POST`
- **Path:** `/api/v1/natural-customer/loans/{loanId}/payments`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`LoanPaymentRequestDTO`):**
```json
{
  "sourceAccountNumber": "CTA-100200300",
  "amount": 250000.00
}
```
- **Response (`LoanPaymentResponseDTO` - HTTP 200 OK)**

### 4.8. Create & Execute Transfer
- **HTTP Method:** `POST`
- **Path:** `/api/v1/natural-customer/transfers`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`CreateTransferRequestDTO`):**
```json
{
  "sourceAccountNumber": "CTA-100200300",
  "destinationAccountNumber": "CTA-900800700",
  "amount": 150000.00,
  "description": "Pago de servicios"
}
```
- **Response (`TransferResponseDTO` - HTTP 201 Created):**
```json
{
  "transferId": "TRF-8801",
  "sourceAccountNumber": "CTA-100200300",
  "destinationAccountNumber": "CTA-900800700",
  "amount": 150000.00,
  "status": "EXECUTED",
  "executedAt": "2026-09-13T10:30:00Z"
}
```

### 4.9. Consult My Operations History
- **HTTP Method:** `GET`
- **Path:** `/api/v1/natural-customer/operations`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`List<OperationResponseDTO>` - HTTP 200 OK)**

---

## 5. Business Customer Endpoints (`BusinessCustomerPort`)

### 5.1. Consult Company Profile
- **HTTP Method:** `GET`
- **Path:** `/api/v1/business-customer/profile`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`BusinessCustomerResponseDTO` - HTTP 200 OK)**

### 5.2. Register Delegated Company User
- **HTTP Method:** `POST`
- **Path:** `/api/v1/business-customer/users`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`RegisterCompanyUserRequestDTO`):**
```json
{
  "username": "operativo_tech",
  "password": "CompanyUserPass123!",
  "role": "BUSINESS_OPERATOR",
  "email": "operativo@techsas.com",
  "identification": "1020304050",
  "name": "Carlos Gomez"
}
```
- **Response (`UserResponseDTO` - HTTP 201 Created)**

### 5.3. Approve Company Transfer
- **HTTP Method:** `PATCH`
- **Path:** `/api/v1/business-customer/transfers/{transferId}/approve`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Request Body:** None
- **Response (`TransferResponseDTO` - HTTP 200 OK)**

### 5.4. Reject Company Transfer
- **HTTP Method:** `PATCH`
- **Path:** `/api/v1/business-customer/transfers/{transferId}/reject`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`RejectTransferRequestDTO`):**
```json
{
  "rejectionReason": "Monto excede el presupuesto autorizado para la semana"
}
```
- **Response (`TransferResponseDTO` - HTTP 200 OK)**

---

## 6. Business Operator Endpoints (`BusinessOperatorPort`)

### 6.1. Create High-Value / Payroll Transfer
- **HTTP Method:** `POST`
- **Path:** `/api/v1/business-operator/transfers`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`CreateTransferRequestDTO`):**
```json
{
  "sourceAccountNumber": "CTA-BUS-5001",
  "destinationAccountNumber": "CTA-900800700",
  "amount": 50000000.00,
  "description": "Pago masivo de nomina quincenal"
}
```
- **Response (`TransferResponseDTO` - HTTP 202 Accepted):**
```json
{
  "transferId": "TRF-9902",
  "amount": 50000000.00,
  "status": "WAITING_FOR_APPROVAL"
}
```

---

## 7. Business Supervisor Endpoints (`BusinessSupervisorPort`)

### 7.1. Consult Pending Approval Transfers
- **HTTP Method:** `GET`
- **Path:** `/api/v1/business-supervisor/transfers/pending`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`List<TransferResponseDTO>` - HTTP 200 OK)**

### 7.2. Approve Pending Transfer
- **HTTP Method:** `PATCH`
- **Path:** `/api/v1/business-supervisor/transfers/{transferId}/approve`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (`TransferResponseDTO` - HTTP 200 OK)**

---

## 8. Teller Employee Endpoints (`TellerEmployeePort`)

### 8.1. Deposit Funds
- **HTTP Method:** `POST`
- **Path:** `/api/v1/teller/accounts/{accountNumber}/deposits`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`DepositRequestDTO`):**
```json
{
  "amount": 500000.00,
  "reference": "Deposito en ventanilla Medellin"
}
```
- **Response (`AccountBalanceResponseDTO` - HTTP 200 OK)**

### 8.2. Withdraw Funds
- **HTTP Method:** `POST`
- **Path:** `/api/v1/teller/accounts/{accountNumber}/withdrawals`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`WithdrawalRequestDTO`):**
```json
{
  "amount": 200000.00,
  "clientIdentification": "1017123456"
}
```
- **Response (`AccountBalanceResponseDTO` - HTTP 200 OK)**

### 8.3. Block Bank Account
- **HTTP Method:** `PATCH`
- **Path:** `/api/v1/teller/accounts/{accountNumber}/block`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`BlockAccountRequestDTO`):**
```json
{
  "reason": "Bloqueo preventivo por solicitud de cliente por perdida de documento"
}
```
- **Response (`BankAccountResponseDTO` - HTTP 200 OK)**

---

## 9. Commercial Employee Endpoints (`CommercialEmployeePort`)

### 9.1. Request Loan on Behalf of Customer
- **HTTP Method:** `POST`
- **Path:** `/api/v1/commercial/loans`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`CommercialRequestLoanRequestDTO`):**
```json
{
  "customerIdentification": "1017123456",
  "loanType": "VEHICLE",
  "requestedAmount": 35000000.00,
  "termInMonths": 48,
  "destinationAccountNumber": "CTA-100200300"
}
```
- **Response (`LoanResponseDTO` - HTTP 201 Created)**

---

## 10. Internal Analyst Endpoints (`InternalAnalystPort`)

### 10.1. Register Internal Employee User
- **HTTP Method:** `POST`
- **Path:** `/api/v1/internal-analyst/users/employee`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`RegisterEmployeeUserRequestDTO`):**
```json
{
  "username": "cajero_ventanilla_1",
  "password": "EmployeePass123!",
  "role": "TELLER_EMPLOYEE",
  "email": "cajero1@banco.com",
  "identification": "1098765432",
  "name": "Maria Rodriguez"
}
```
- **Response (`UserResponseDTO` - HTTP 201 Created)**

### 10.2. Change Customer Operational Status
- **HTTP Method:** `PATCH`
- **Path:** `/api/v1/internal-analyst/customers/{identification}/status`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`ChangeCustomerStatusRequestDTO`):**
```json
{
  "status": "BLOCKED",
  "reason": "Investigacion preventiva por prevencion de lavado de activos"
}
```
- **Response (`CustomerResponseDTO` - HTTP 200 OK)**

### 10.3. Approve Loan Request
- **HTTP Method:** `PATCH`
- **Path:** `/api/v1/internal-analyst/loans/{loanId}/approve`
- **Headers:** `Authorization: Bearer <jwt_token>`, `Content-Type: application/json`
- **Request Body (`ApproveLoanRequestDTO`):**
```json
{
  "approvedAmount": 35000000.00,
  "interestRate": 1.45
}
```
- **Response (`LoanResponseDTO` - HTTP 200 OK)**

### 10.4. Disburse Approved Loan
- **HTTP Method:** `POST`
- **Path:** `/api/v1/internal-analyst/loans/{loanId}/disburse`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Request Body:** None
- **Response (`LoanResponseDTO` - HTTP 200 OK)**

### 10.5. Consult Immutable Audit Log (MongoDB)
- **HTTP Method:** `GET`
- **Path:** `/api/v1/internal-analyst/audit-logs`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Query Parameters:** `?userId=usr_1001&operationType=LOAN_DISBURSEMENT&page=0&size=20`
- **Response (`AuditLogPageResponseDTO` - HTTP 200 OK):**
```json
{
  "content": [
    {
      "auditId": "65f8a123b04c9e",
      "operationType": "LOAN_DISBURSEMENT",
      "operationDate": "2026-09-13T11:00:00Z",
      "performedBy": "usr_analyst_01",
      "userRole": "INTERNAL_ANALYST",
      "affectedProduct": "LOAN-5501",
      "details": {
        "disbursedAmount": 35000000.00,
        "destinationAccount": "CTA-100200300"
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 10.6. Delete / Cancel Resource
- **HTTP Method:** `DELETE`
- **Path:** `/api/v1/internal-analyst/loans/{loanId}`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Response (HTTP 204 No Content)**
