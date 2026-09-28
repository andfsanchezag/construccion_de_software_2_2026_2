# Global Exception Handler Specification (Spring Boot)

## 1. Purpose

This document defines the single error contract for all REST endpoints and the Spring Security boundary. Domain exceptions remain framework-independent; the REST/infrastructure layer maps them to HTTP responses.

## 2. Standard Error Response

All handled errors use this envelope:

```json
{
  "timestamp": "2026-09-23T12:00:00.000Z",
  "status": 404,
  "code": "RESOURCE_NOT_FOUND",
  "message": "Requested resource was not found",
  "path": "/api/v1/customers/1017",
  "requestId": "req-01H...",
  "details": null
}
```

`code` is stable and machine-readable; clients must not parse `message`. The message and `details` must be safe for the client. `path` excludes query values. Unexpected errors return a generic message and never expose stack traces, SQL, credentials, tokens, or internal hostnames.

## 3. Exception Mapping

| Failure | HTTP | Code |
|---|---:|---|
| Malformed JSON, DTO binding/validation, invalid request shape | 400 | `INVALID_REQUEST` or documented `INVALID_<FIELD>` |
| Missing/invalid bearer token or invalid credentials | 401 | `AUTHENTICATION_REQUIRED` / `INVALID_CREDENTIALS` |
| Authenticated user lacks role or business scope | 403 | `FORBIDDEN` |
| Resource/reference does not exist | 404 | `RESOURCE_NOT_FOUND` or a stable resource-specific code |
| Duplicate resource or illegal state transition | 409 | `RESOURCE_ALREADY_EXISTS` / `INVALID_STATE_TRANSITION` |
| Required database/service dependency unavailable | 503 | `DEPENDENCY_UNAVAILABLE` |
| Unclassified programming/runtime failure | 500 | `INTERNAL_ERROR` |

Unknown exceptions must never fall back to `400`. Only known uniqueness violations map to `409`; do not translate every persistence exception to a client error.

## 4. Spring Boot Implementation

- Implement a global `@RestControllerAdvice` for controller, DTO validation, domain, and persistence exceptions.
- Configure the Spring Security `AuthenticationEntryPoint` and `AccessDeniedHandler` to return the same envelope for `401` and `403`; `@RestControllerAdvice` alone does not handle filter-chain failures.
- Use a shared error response DTO and a centralized exception-to-code mapping.
- Reuse or generate `X-Request-Id`, include the same value as `requestId` in the body, and include it in server-side logs.
- Log unexpected failures internally with the request ID; never serialize exception messages or stack traces by default.
- Do not write a second response after the response is committed. Preserve the original exception as the cause for diagnostics and tests.

## 5. Acceptance Tests

Tests must verify the envelope and status/code mapping for malformed JSON, DTO validation, invalid credentials, missing token, forbidden role, not found, duplicate/conflict, unavailable dependency, and unexpected exception. Include filter-chain tests proving `401`/`403` use the same envelope and request-id tests proving the header and body correlate.