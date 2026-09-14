# REST Adapters Specification

## 1. Overview

REST Adapters reside in `application/adapters/rest/`. They handle HTTP transport concerns, receiving client requests, validating HTTP DTOs, mapping requests to Domain Models, evaluating authentication and user roles, calling the corresponding **Role Input Port**, and mapping Domain Models back to HTTP Response DTOs.

---

## 2. Component Architecture

```text
HTTP Request (Client)
      |
      v  Contains JWT Token
Security Filter / Middleware
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
   - `sub` / `userId`: Unique User ID.
   - `username`: Account username.
   - `role`: System role (e.g. `NATURAL_CUSTOMER`, `INTERNAL_ANALYST`, etc.).
   - `email`: User email address.
   - `identification`: DNI / NIT document.
   - `customer`: Associated customer profile information.

### 3.2 JWT Extraction & User Reconstruction
For every protected HTTP request:
1. The **Security Filter / Interceptor** intercepts the `Authorization: Bearer <token>` header.
2. It verifies the signature and extracts claims (`userId`, `username`, `role`, `email`, `identification`, `customer`).
3. It reconstructs a complete `User` Domain Model from these claims.
4. The reconstructed `User` object is injected into the controller context (or request scope).
5. The REST class evaluates the user's `SystemRole` against the targeted Role Input Port before invoking the Use Case.
6. The REST controller passes the reconstructed `User` domain model to the Input Port method so that domain services have full user context for business rules and queries.

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
            @AuthenticationPrincipal User authenticatedUser, // User reconstructed from JWT
            @RequestBody LoanRequestDTO requestDTO) {

        // 1. Mapeo RequestDTO -> Domain Model
        Loan loanModel = LoanRestMapper.toDomain(requestDTO);

        // 2. Invocación del Caso de Uso (Input Port) pasando el User reconstruido
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

import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String token = extractBearerToken(request);
        if (token != null && jwtProvider.validateToken(token)) {
            Claims claims = jwtProvider.getClaims(token);
            
            // Reconstrucción del objeto de dominio User desde las claims del JWT
            User userDomain = new User();
            userDomain.setUserId(claims.get("userId", String.class));
            userDomain.setUsername(claims.getSubject());
            userDomain.setRole(SystemRole.valueOf(claims.get("role", String.class)));
            userDomain.setEmail(claims.get("email", String.class));
            
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(userDomain, null, userDomain.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 6. Code Pattern Example (TypeScript / NestJS / Express)

### A. Controller Pattern
```typescript
import { Controller, Post, Body, UseGuards, Req } from '@nestjs/common';
import { NaturalCustomerPort } from '../../../domain/ports/in/NaturalCustomerPort';
import { LoanRestMapper } from '../mappers/LoanRestMapper';
import { LoanRequestDTO } from '../dtos/requests/LoanRequestDTO';
import { LoanResponseDTO } from '../dtos/responses/LoanResponseDTO';
import { JwtAuthGuard } from '../../../infrastructure/security/JwtAuthGuard';

@Controller('api/v1/natural-customer/loans')
@UseGuards(JwtAuthGuard)
public class NaturalCustomerLoanController {

  constructor(private readonly naturalCustomerPort: NaturalCustomerPort) {}

  @Post()
  async requestLoan(@Req() req: any, @Body() dto: LoanRequestDTO): Promise<LoanResponseDTO> {
    // 1. User domain model reconstructed from JWT payload by JwtAuthGuard
    const authenticatedUser = req.user;

    // 2. Map RequestDTO -> Domain Model
    const loanModel = LoanRestMapper.toDomain(dto);

    // 3. Call Use Case Input Port
    const requestedLoan = await this.naturalCustomerPort.requestLoan(authenticatedUser, loanModel);

    // 4. Map Domain Model -> ResponseDTO
    return LoanRestMapper.toResponseDTO(requestedLoan);
  }
}
```

### B. JWT Strategy / Guard (User Reconstruction)
```typescript
import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { User } from '../../../domain/models/User';
import { SystemRole } from '../../../domain/valueobjects/SystemRole';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKey: process.env.JWT_SECRET,
    });
  }

  async validate(payload: any): Promise<User> {
    // Reconstruct User domain model from JWT payload claims
    const user = new User();
    user.userId = payload.userId;
    user.username = payload.sub;
    user.role = payload.role as SystemRole;
    user.email = payload.email;
    user.identification = payload.identification;
    return user;
  }
}
```
