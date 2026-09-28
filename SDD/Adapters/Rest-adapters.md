# REST Adapters Specification

## 1. Overview

REST Adapters reside in `application/adapters/rest/`. They handle HTTP transport concerns, receiving client requests, validating HTTP DTOs, mapping requests to Domain Models, evaluating authentication and user roles, calling the corresponding **Role Input Port**, and mapping Domain Models back to HTTP Response DTOs.

---

## 2. Component Architecture

```text
HTTP Request (Client)
      |
      v  Contains JWT Token
Spring Security Filter Chain
      |  Extracts Claims & Reconstructs User Domain Model
      v
REST Controller (adapters/rest/controllers)
      |  1. Mapea RequestDTO -> Domain Model
      |  2. Calls Use Case Input Port (passing User + Domain Model)
      v
Role Input Port Interface (domain/ports/in/*Port)
      ^
      |  Implemented by UseCase in adapters/useCases/
Use Case Implementation
```

---

## 3. Authentication, JWT & User Reconstruction Flow

### 3.1 Login Endpoint (`/api/v1/auth/login`)
1. The client sends credentials via `LoginRequestDTO`.
2. The controller calls `PublicAccessPort.login(userModel)`.
3. Upon successful credential validation, the system issues a **JWT Token**.
4. **JWT Payload Contents:**
    - `sub`: Immutable internal user identifier.
    - `jti`: Unique token identifier.
    - `ver`: Current persisted `User.authTokenVersion` used to invalidate previous sessions.
    - `iat` and `exp`: Issuance and expiration timestamps.
    - Do not include passwords, email, identification, customer profiles, or authorization snapshots in claims.

### 3.2 JWT Extraction & User Reconstruction
For every protected HTTP request:
1. The **Security Filter / Interceptor** intercepts the `Authorization: Bearer <token>` header.
2. It verifies the signature, issuer/audience where configured, and expiration, then extracts only the subject and token identifier.
3. A Spring Security `UserDetailsService` loads the current user by subject through `UserRepositoryPort`; it rejects missing, inactive, or blocked users and rejects a token whose `ver` differs from the user's current `authTokenVersion`. Current role and customer association come from this authoritative Domain Model, not JWT claims.
4. The security adapter creates an `AuthenticatedUserPrincipal` that contains the loaded `User` Domain Model and Spring `GrantedAuthority` values. Spring Security types remain outside `domain/`.
5. The REST controller obtains that principal and passes its `User` Domain Model to the Role Input Port. The role guard must use the loaded user's current role and the ownership/business-scope checks defined by the domain.

---

## 4. REST Layer Structure

```text
adapters/rest/
├── controllers/            <-- Endpoints organized by Role / Feature
├── dtos/
│   ├── requests/           <-- Incoming HTTP Payloads
│   └── responses/          <-- Outgoing HTTP Payloads
└── mappers/                <-- DTO <-> Domain Model Converters
```

---

## 5. Code Pattern Example (Java / Spring Boot)

### A. Controller Pattern
```java
package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.LoanRequestDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.adapters.rest.mappers.LoanRestMapper;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.ports.in.NaturalCustomerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/natural-customer/loans")
public class NaturalCustomerLoanRestController {

    private final NaturalCustomerPort naturalCustomerPort;

    public NaturalCustomerLoanRestController(NaturalCustomerPort naturalCustomerPort) {
        this.naturalCustomerPort = naturalCustomerPort;
    }

    @PostMapping
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestBody LoanRequestDTO requestDTO) {

        User authenticatedUser = principal.domainUser();

        // 1. Mapeo RequestDTO -> Domain Model
        Loan loanModel = LoanRestMapper.toDomain(requestDTO);

        // 2. Invocación del Caso de Uso (Input Port) pasando el usuario autenticado
        Loan requestedLoan = naturalCustomerPort.requestLoan(authenticatedUser, loanModel);

        // 3. Mapeo Domain Model -> ResponseDTO
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LoanRestMapper.toResponseDTO(requestedLoan));
    }
}
```

### B. JWT Authentication Filter & User Reconstruction Pattern
```java
package application.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final SecurityUserLoader securityUserLoader;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, SecurityUserLoader securityUserLoader) {
        this.jwtProvider = jwtProvider;
        this.securityUserLoader = securityUserLoader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String token = extractBearerToken(request);
        if (token != null && jwtProvider.validateToken(token)) {
            Claims claims = jwtProvider.getClaims(token);
            
            // Load current status, role, and customer relationship from authoritative storage.
                AuthenticatedUserPrincipal principal = securityUserLoader.loadActiveUser(
                    claims.getSubject(), claims.get("ver", Long.class));
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

---

