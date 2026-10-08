# Sistema Bancario — Construcción de Software 2 (2026-2)

Aplicación bancaria en **Java 17 + Spring Boot 4.1** con **arquitectura hexagonal**
(DDD + Ports & Adapters), desarrollada a partir de la especificación del
`SDD/` (Software Design Document). Incluye banca por roles, JWT con
invalidación de sesiones, persistencia relacional en **MySQL** y auditoría en
**MongoDB**.

> **Para poner el proyecto en marcha paso a paso, ver [`SETUP.md`](SETUP.md).**

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje / Build | Java 17, Maven (wrapper `bank/mvnw.cmd` o contenedor Docker) |
| Framework | Spring Boot 4.1.0, Spring MVC, Spring Security (JWT stateless, BCrypt) |
| Persistencia relacional | Spring Data JPA / Hibernate → MySQL 8 (`bank_db`, puerto 3306, DDL automático) |
| Auditoría NoSQL | Spring Data MongoDB → MongoDB (`audit_db.audit_logs`, puerto 27017) |
| JWT | JJWT 0.12.6 (`sub`, `jti`, `ver`, `iat`, `exp`; sin PII) |
| Validación / errores | Jakarta Bean Validation, `@RestControllerAdvice` con envelope + `X-Request-Id` |
| Observabilidad | Spring Boot Actuator (`/actuator/health`) |
| Pruebas | JUnit 5 — `docker compose exec bank-app mvn test` |

## Estructura del repositorio

```text
.
├── bank/                  # Aplicación Spring Boot (código + tests + resources)
│   ├── Dockerfile          # JDK 17 + Maven para ejecutar la app en contenedor
│   └── src/main/java/application/
│       ├── domain/        # Núcleo puro: models, valueobjects, enums, exceptions,
│       │                  #   ports/in (8 roles + acceso público), ports/out, services
│       ├── adapters/      # persistence (jpa, mongodb), rest (controllers, dtos,
│       │                  #   mappers, exception), useCases (implementan puertos de entrada)
│       └── infrastructure/ # security (JWT, BCrypt, CORS), config, notification
├── SDD/                   # Especificación: enunciados, arquitectura, dominio,
│                          #   adapters y el prompt del agente orquestador
├── docker-compose.yml     # App + MySQL 3306 + MongoDB 27017
├── SETUP.md               # Guía paso a paso para ejecutar el proyecto
└── LICENSE                # MIT
```

## Seguridad y roles

- Autenticación JWT sin sesión. El token solo lleva identidad (`sub` = id interno,
  `jti`, `ver` = `User.authTokenVersion`); rol y permisos se resuelven siempre del
  modelo vigente en base de datos.
- Cambiar contraseña, cambiar estado o hacer logout incrementa la versión y
  **invalida todos los tokens anteriores** (verificado en vivo: token viejo → 403).
- 8 puertos de entrada por rol (`/api/v1/teller`, `/api/v1/natural-customer`,
  `/api/v1/business-customer`, `/api/v1/business-operator`,
  `/api/v1/business-supervisor`, `/api/v1/commercial`,
  `/api/v1/internal-analyst`) más acceso público (`/api/v1/auth`).
- Semántica HTTP verificada: 200/201/204 en éxito, 400 validación, 401
  credenciales inválidas, 403 sin token o con token obsoleto, 404 con envelope.
- CORS restringido por `FRONTEND_ORIGIN` (default `http://localhost:5173`).

## Variables de entorno

| Variable | Default | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/bank_db?...` | JDBC MySQL (en Compose: `mysql-db`) |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root_password` | Credenciales MySQL |
| `MONGODB_URI` | `mongodb://localhost:27017/audit_db` | URI Mongo (en Compose: `mongo-db`) |
| `JWT_SECRET` | `TDEATDEATDEATDEATDEATDEATDEATDEA` | Clave HMAC de firma del JWT (default netamente educativo/académico; sobrescríbela si expones el servicio fuera de tu máquina) |
| `JWT_EXPIRATION_MS` | `3600000` | Vigencia del JWT en milisegundos |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Origen CORS permitido |
| `SERVER_PORT` | `8080` | Puerto HTTP de la app |

## Estado de validación

- [x] Compilación limpia + 110/110 tests (dominio, servicios, JWT, seguridad, persistencia fakes)
- [x] Auto-DDL verificado: tablas en MySQL y colección `audit_logs` creadas al arrancar
- [x] Flujo live verificado: registro → login → endpoint protegido → logout → token viejo rechazado
- [x] Push a `main` al día

## Notas

- Spring Boot 4.x usa el prefijo **`spring.mongodb.uri`** (no `spring.data.mongodb.uri`).
- Si el puerto 8080 está ocupado (p. ej. Docker Desktop), arrancar con
  `SERVER_PORT=8081`.
- Ver solución de problemas comunes en [`SETUP.md`](SETUP.md#6-solución-de-problemas).
