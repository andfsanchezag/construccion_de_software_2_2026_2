# C4 Nivel 3 — Componentes

## Propósito

Abrir `bank-app` y mostrar los componentes que toca **cada endpoint**:
controladores REST, casos de uso, servicios de dominio, puertos y
adaptadores de salida.

## Diagrama

```mermaid
C4Component
    title Componentes — bank-app (hexágono)

    Container_Boundary(app, "bank-app") {
        Component(filter, "JwtAuthenticationFilter", "Spring OncePerRequestFilter", "Valida JWT y fija AuthenticatedUserPrincipal")
        Component(secfg, "SecurityConfig", "Spring Security", "Reglas hasRole por prefijo /api/v1/**, CORS, BCrypt")
        Component(rest, "REST Controllers ×8", "Spring MVC", "Auth, Natural, Business, Operator, Supervisor, Teller, Commercial, Analyst")
        Component(rmap, "REST DTOs + Mappers", "Jakarta Validation", "Request/Response DTOs y conversión DTO ↔ dominio")
        Component(uc, "UseCases ×8", "Spring @Service", "Implementan ports/in y orquestan servicios de dominio")
        Component(svc, "Domain Services", "Java puro", "account, customer, loan, transfer, operation, user, authorization")
        Component(pin, "Ports In", "Interfaces", "PublicAccess, Natural, Business, Operator, Supervisor, Teller, Commercial, Analyst")
        Component(pout, "Ports Out", "Interfaces", "User, Customer, BankAccount, Loan, Transfer, Operation, AuditLog, Jwt, Password, Config, Notification")
        Component(jpa, "JPA Adapters ×6", "Spring Data JPA", "User, Customer, BankAccount, Loan, Transfer, Operation → MySQL")
        Component(mongo, "AuditLogMongoAdapter", "Spring Data Mongo", "AuditLog → audit_logs")
        Component(jwt, "JwtProvider + JwtClaimsMapper", "JJWT", "Emite/valida tokens, mapea User ↔ JwtClaimsDTO")
        Component(seed, "DatabaseSeeder", "CommandLineRunner", "Semillas idempotentes de demo")
    }

    Rel(rest, filter, "Precede (cadena de filtros)")
    Rel(filter, jwt, "validateToken / reconstructUser")
    Rel(rest, rmap, "DTO ↔ dominio")
    Rel(rest, pin, "Invoca puerto in con User autenticado")
    Rel(uc, pin, "Implementa")
    Rel(uc, svc, "Delega regla de negocio")
    Rel(svc, pout, "Persiste / emite / configura / notifica")
    Rel(jpa, pout, "Implementa (MySQL)")
    Rel(mongo, pout, "Implementa (MongoDB)")
    Rel(jwt, pout, "Implementa JwtServicePort")
```

## Ciclo de vida de una petición (vale para los ~50 endpoints)

```mermaid
sequenceDiagram
    participant F as JwtAuthenticationFilter
    participant C as Controller (rol)
    participant M as REST Mapper
    participant P as Port In
    participant U as UseCaseImpl
    participant S as Domain Service
    participant O as Port Out
    participant A as Adapter (JPA/Mongo/Security)
    F->>C: User reconstruido del JWT (o 403 si falta/inválido)
    C->>M: RequestDTO → modelo dominio (stub con identificadores)
    C->>P: port.metodo(authenticatedUser, modelo...)
    P->>U: dispatch
    U->>S: ejecuta regla de negocio
    S->>O: find/save/update + validaciones/autorización
    O->>A: JPA/Mongo/JWT/Password/Config/Notification
    A-->>C: modelo resultante
    C->>M: modelo → ResponseDTO
    C-->>SPA: 200/201/204
```

Si el servicio lanza excepción de dominio, `GlobalExceptionHandler`
la traduce a envelope (`400/401/403/404/409`); si falla Bean Validation,
`400` antes de tocar el dominio. Detalle por capa: `05`, `06`, `07`, `08`,
`09`, `10`. Detalle por endpoint: `11-endpoints-flujos.md`.
