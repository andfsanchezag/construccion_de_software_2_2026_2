# REST Validation Specification (Spring Boot)

## 1. Purpose

This document defines request validation at the REST boundary. It applies to every endpoint in `SDD_cs2/Adapters/Api-rest-endpoints.md` and does not replace domain validation in `SDD_cs2/Domain/services/`.

Use Spring MVC and Jakarta Bean Validation (`jakarta.validation`) on request DTOs. Controllers must apply `@Valid`; request DTOs and Spring annotations must remain outside the Domain layer.

## 2. Processing Order

For protected routes, the Spring Security filter chain authenticates the request and checks the required role before controller execution. After security succeeds, the REST boundary validates in this order:

1. Transport: HTTP method/path, JSON content type, well-formed JSON, path/query parsing.
2. Shape: required fields, JSON types, and presence; reject `null`, blank required strings, and unintended type coercion.
3. Format: email, phone, dates, identifiers, catalog codes, and numeric ranges.
4. Cross-field constraints: relationships and request-level consistency.
5. Domain services: authoritative ownership, balance, eligibility, state-transition, and approval rules.

A failure stops processing. DTO validation must not duplicate authoritative domain rules or permit the controller to mutate persisted entities directly.

## 3. Shared Field Rules

| Field | Boundary rule |
|---|---|
| `identification` | Required where declared; 1-30 characters; no surrounding whitespace. |
| `username` | Required; 3-40 characters; no whitespace. |
| `password` | Required for registration/login; 8-100 characters; never returned in responses or logged. |
| `email` | Valid email with non-empty local and domain parts; unique conflicts are handled by the service as `409`. |
| `phoneNumber` | 7-15 digits, optional leading `+`. |
| `name` | 1-120 non-blank characters. |
| `address` | 1-200 non-blank characters when required. |
| `accountNumber` | 1-30 characters; no whitespace. |
| `loanId`, `transferId`, `userId` | Required path identifiers; non-blank and within the identifier format defined by the Domain Model. |
| catalog fields | Exact supported `fromCode` value; case-sensitive; unknown values rejected. |
| date | ISO-8601 (`YYYY-MM-DD` or timestamp where the contract requires time); must parse as a real date. |
| amount | JSON decimal, finite and greater than zero. Numeric strings are rejected. |
| `termInMonths` | Positive integer. |
| `page`, `size` | `page >= 0`; `size >= 1`, default 20 and maximum 100. |

Natural-customer age, account ownership, available balance, approval threshold, and state transitions are domain rules; DTO annotations alone must not be treated as enforcing them.

## 4. Endpoint-Specific Rules

- Login requires non-blank `username` and `password`. Unknown username and incorrect password return the same `401 INVALID_CREDENTIALS` response.
- Natural and business customer registration require the fields shown in the API contract. Duplicate identification/email returns `409 RESOURCE_ALREADY_EXISTS`; an unknown legal representative returns `404 RESOURCE_NOT_FOUND`.
- Profile updates accept only editable fields and require at least one field. Identity, role, status, balances, and approval data are not client-editable.
- Loan requests require a supported `loanType`, positive `requestedAmount`, positive integer `termInMonths`, and a destination account reference. The service verifies that the account is active and belongs to the applicant.
- Transfer creation requires distinct source and destination accounts and a positive amount. The service verifies ownership/scope, active account states, available funds, approval threshold, and atomic balance updates.
- Approval/rejection requests must target an existing item in an eligible state. Rejection reasons are required only where the endpoint DTO specifies one and are 1-500 non-blank characters.
- Account opening requires an existing eligible owner, supported account type/currency, and a unique account number.
- Invalid enum/catalog codes, malformed JSON, missing required fields, and invalid formats return `400 INVALID_REQUEST` (or a documented deterministic `INVALID_<FIELD>` code). Duplicate resources return `409`; unknown referenced resources return `404`.

## 5. Spring Implementation Requirements

- Apply `@Valid` to request DTO parameters and use Jakarta constraints such as `@NotBlank`, `@Email`, `@Size`, `@Positive`, and `@PositiveOrZero` where applicable.
- Use class-level validators for cross-field DTO constraints; keep ownership, account balances, eligibility, and state transitions in domain services.
- Configure Jackson to reject malformed JSON and unexpected scalar coercions for security-sensitive identifiers, booleans, and numeric fields.
- Convert binding and validation exceptions through `@RestControllerAdvice` as specified by `SDD_cs2/Adapters/Global-exception-handler.md`.
- Do not return persistence entities, exception internals, passwords, or secret values in validation responses.

## 6. Acceptance Tests

Tests must cover malformed JSON, missing and blank fields, invalid formats/catalog codes, numeric boundaries, date parsing, unknown references, duplicate resources, and the distinction between `400`, `401`, `403`, `404`, and `409`. At least one test must prove invalid requests do not invoke the domain use case.