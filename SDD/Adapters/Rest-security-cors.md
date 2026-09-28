# REST CORS and Browser Security Specification (Spring Security)

## 1. Purpose

This document defines the browser security boundary for the frontend consuming the Banking Information Management System API.

## 2. CORS Policy

- Allow only the configured `FRONTEND_ORIGIN`; the development default is `http://localhost:5173`.
- Production origins must be explicitly configured and must not inherit development defaults.
- Allow only the documented methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, and `OPTIONS`.
- Allow headers `Authorization`, `Content-Type`, `Accept`, and `X-Request-Id`; expose `X-Request-Id`.
- Set `allowCredentials=false` while JWTs are sent in the `Authorization` header and are not cookies.
- Never combine wildcard origins with authorization headers or credentials. Reject disallowed origins; do not reflect arbitrary `Origin` values.
- Accept CORS preflight `OPTIONS` with `204` (or the Spring equivalent) without requiring JWT. Protected business methods remain authenticated and role-authorized.

If authentication later moves to secure HttpOnly cookies, explicitly enable credentials only for the configured origin and add CSRF protection.

## 3. Spring Security Configuration

Configure CORS centrally through the `SecurityFilterChain` using a `CorsConfigurationSource` (or an equivalent single `WebMvcConfigurer` integration). Avoid competing CORS configurations. Read `FRONTEND_ORIGIN` from environment-based configuration.

CORS does not replace JWT authentication, current-user status checks, or role/business-scope authorization. Never accept passwords or tokens in query parameters.

## 4. Request Correlation and Production

For every request, accept a valid `X-Request-Id` or generate one, return it in the response header, include it in server logs, and include it as `requestId` in errors according to `SDD_cs2/Adapters/Global-exception-handler.md`.

Production deployment requires HTTPS, externally managed secrets, and rate limiting. Error responses must not expose stack traces or database details.

## 5. Acceptance Tests

Tests must prove that the configured frontend origin receives the expected CORS headers; preflight succeeds for the documented methods/headers without JWT; an unapproved origin receives no permissive CORS headers; protected requests without a token return `401`; wrong roles return `403`; approved-origin requests preserve `X-Request-Id`; and wildcard origins are never enabled.