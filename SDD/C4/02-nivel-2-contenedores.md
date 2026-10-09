# C4 Nivel 2 — Contenedores

## Propósito

Mostrar dónde se ejecuta cada parte y cómo viaja una petición de endpoint
desde el navegador hasta la base de datos.

## Diagrama

```mermaid
C4Container
    title Contenedores — Sistema Bancario

    Person(usuario, "Usuario por rol", "Navegador con JWT en memoria")

    System_Boundary(boundary, "Plataforma (docker-compose)") {
        Container(spa, "Frontend SPA", "Vite/React", "Origen FRONTEND_ORIGIN, guarda el JWT y lo envía como Bearer")
        Container(app, "bank-app", "Spring Boot 4.1 + Java 17", "Hexágono: rest → useCases → domain → persistence/security. Puerto 8080 (SERVER_PORT)")
        ContainerDb(mysql, "mysql-db", "MySQL 8 / bank_db :3306", "Tablas JPA: users, customers, accounts, loans, transfers, operations")
        ContainerDb(mongo, "mongo-db", "MongoDB / audit_db :27017", "Colección audit_logs (inmutable)")
    }

    Rel(usuario, spa, "Usa")
    Rel(spa, app, "JSON/HTTPS :8080, Authorization: Bearer <jwt>, X-Request-Id")
    Rel(app, mysql, "JDBC DB_URL/DB_USERNAME/DB_PASSWORD, DDL auto")
    Rel(app, mongo, "MONGODB_URI (spring.mongodb.uri)")
```

## Contenedores y configuración

| Contenedor | Imagen / arranque | Env relevantes | Papel en los endpoints |
|---|---|---|---|
| `spa` | Fuera de este repo (Vite, `:5173`) | `FRONTEND_ORIGIN` del lado app | Dispara todos los flujos; preflight `OPTIONS` sin JWT |
| `bank-app` | `bank/Dockerfile` (JDK 17 + Maven) | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MONGODB_URI`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `FRONTEND_ORIGIN`, `SERVER_PORT` | Atiende `POST/GET/PUT/PATCH/DELETE /api/v1/**`, valida JWT, ejecuta dominio, persiste |
| `mysql-db` | MySQL 8 | `bank_db` | Lecturas/escrituras de todos los endpoints salvo auditoría |
| `mongo-db` | MongoDB | `audit_db.audit_logs` | Solo `GET /api/v1/internal-analyst/audit-logs` (lectura) y escrituras internas de auditoría |

## Flujo de red por endpoint (todos los grupos)

```mermaid
sequenceDiagram
    participant SPA as SPA
    participant APP as bank-app :8080
    participant MY as MySQL
    participant MO as MongoDB
    SPA->>APP: HTTP (GET/POST/PUT/PATCH/DELETE) /api/v1/** + Bearer (salvo /auth/login y registros)
    APP->>APP: JwtAuthenticationFilter valida firma/exp y reconstruye User del token
    APP->>APP: SecurityConfig autoriza por ROLE_* según prefijo de ruta
    APP->>MY: JPA (lectura/escritura del agregado del endpoint)
    APP->>MO: AuditLogMongoAdapter.save (solo operaciones auditables)
    APP-->>SPA: 200/201/204 + DTO o envelope de error + X-Request-Id
```

Semántica verificada: `200/201/204` éxito · `400` validación ·
`401` credenciales inválidas · `403` sin token / token obsoleto / rol
incorrecto · `404` con envelope.
