# Services

## Introduction

This document provides a conceptual overview of the services that compose the Banking Information Management System.

The services described here define the main business capabilities exposed by the system. At this level, each service is described only in terms of its purpose and responsibility within the domain.

The detailed definition of each service—including inputs, outputs, business rules, validations, authorization requirements, domain interactions, exceptions, persistence considerations, and technical implementation—will be documented in separate files organized by **subdomain**.

The service documentation is therefore divided conceptually into the following subdomains:

- **Customer Management**
- **User and Authentication Management**
- **Bank Account Management**
- **Loan Management**
- **Transfer Management**
- **Operation and Audit Management**
- **Authorization**

---

# Customer Management Services

## Register Natural Customer

Creates a new banking customer representing a natural person and establishes the customer's initial information and banking status.

## Register Business Customer

Creates a new banking customer representing a legal business entity and associates its legal representative.

## Consult Customer

Retrieves the information of a banking customer according to the access permissions of the requesting user.

## Update Customer

Updates the information maintained for an existing banking customer according to the applicable business rules.

## Change Customer Status

Changes the operational status of a customer, such as activating, deactivating, or blocking the customer's banking relationship.

## Consult Customer Products

Retrieves the banking products and services associated with a customer, such as accounts, loans, and transfers.

---

# User and Authentication Management Services

## Register Customer User

Creates a system user associated with an existing `Customer`.

This service allows a customer-related system identity to be created without requiring the user to be an internal bank employee.

## Register Employee User

Creates a system user representing an internal bank employee.

This service is restricted to the `INTERNAL_ANALYST` role according to the business authorization rules.

## Login

Authenticates a system user using their registered credentials and establishes an authenticated session.

## Logout

Terminates the authenticated user's current session and prevents further operations through that session.

## Consult User

Retrieves information about a system user according to the permissions of the requesting user.

## Change User Status

Changes the status of a system user's access to the application, such as activating, deactivating, or blocking the user.

---

# Bank Account Management Services

## Open Bank Account

Creates a new bank account associated with a customer and establishes its initial configuration and status.

## Consult Bank Account

Retrieves information about a bank account according to the permissions of the requesting user.

## Consult Balance

Retrieves the current available balance of a bank account.

## Deposit Funds

Adds funds to a bank account and generates the corresponding business operation and audit record.

## Withdraw Funds

Removes funds from a bank account after validating the applicable account and transaction conditions.

## Block Bank Account

Changes the operational status of a bank account to blocked and records the corresponding business operation.

## Unblock Bank Account

Restores a blocked bank account to an operational state and records the corresponding business operation.

## Close Bank Account

Permanently closes a bank account according to the applicable business rules and records the corresponding operation.

---

# Loan Management Services

## Request Loan

Creates a loan request for a customer and starts the corresponding loan lifecycle.

## Consult Loan

Retrieves information about a loan or loan request according to the requesting user's permissions.

## Approve Loan

Approves a loan request after applying the required business validations and authorization rules.

## Reject Loan

Rejects a loan request and records the corresponding decision and operation.

## Disburse Loan

Transfers the approved loan amount to the designated destination account and records the corresponding business operation and audit event.

## Register Loan Payment

Registers a payment made against an existing loan and updates the corresponding loan information.

## Close Loan

Completes the loan lifecycle when the applicable conditions for closure have been satisfied.

---

# Transfer Management Services

## Create Transfer

Creates a transfer request between two bank accounts and establishes its initial state.

## Execute Transfer

Executes an authorized transfer by moving funds from the source account to the destination account.

## Submit Transfer for Approval

Places a transfer into an approval state when the applicable business rules require authorization before execution.

## Approve Transfer

Approves a transfer that requires authorization and allows it to proceed according to the applicable business rules.

## Reject Transfer

Rejects a transfer awaiting approval and records the corresponding business operation.

## Expire Transfer

Marks a transfer as expired when it remains awaiting approval beyond the permitted approval period.

---

# Operation and Audit Management Services

## Register Operation

Creates a business operation representing a significant action performed over a banking product or service.

Operations provide traceability between the user performing an action and the affected banking product.

## Consult Operations

Retrieves the operations associated with banking products according to the requesting user's permissions.

## Register Audit Event

Creates an immutable audit record for a significant business operation.

The audit record preserves information such as the operation, user, role, affected product, timestamp, and operation-specific details.

## Consult Audit Log

Retrieves historical audit records according to the access permissions of the requesting user.

---

# Authorization Services

## Validate Permissions

Determines whether a user has permission to perform a specific business operation based on the user's role and status.

## Validate Customer Access

Determines whether a user is authorized to access information belonging to a specific customer.

## Validate Product Access

Determines whether a user is authorized to access or operate on a specific banking product or service.

## Validate Approval Authorization

Determines whether a user has the required authority to approve a business operation, such as a loan or a transfer requiring approval.

---

# Service Organization

The services described in this document provide the **high-level service catalog** of the system.

They intentionally do not describe implementation details or complete business workflows.

Detailed specifications will be maintained in separate Markdown files organized by subdomain. For example:

```text
services/
│   └── customer-services.md
│
│   └── user-authentication-services.md
│
│   └── bank-account-services.md
│
│   └── loan-services.md
│
│   └── transfer-services.md
│
│   └── operation-audit-services.md
│
    └── authorization-services.md