# C4 Nivel 1 — Contexto

## Propósito

Delimitar qué es el sistema, quién lo usa y con qué sistemas externos se
relaciona. Todo el comportamiento bancario vive dentro del sistema; fuera
solo hay actores humanos (vía frontend), bases de datos y el navegador.

## Diagrama

```mermaid
C4Context
    title Contexto — Sistema Bancario (Construcción de Software 2)

    Person(publico, "Público", "Registro y login sin autenticar")
    Person(natural, "Cliente natural", "Autogestión: perfil, cuentas, préstamos, transferencias")
    Person(biz, "Cliente empresarial", "Perfil empresa, usuarios delegados, aprueba/rechaza")
    Person(operador, "Operador empresarial", "Crea transferencias de alto valor / nómina")
    Person(supervisor, "Supervisor empresarial", "Aprueba / rechaza transferencias pendientes")
    Person(teller, "Cajero (teller)", "Ventanilla: depósitos, retiros, bloqueo, apertura")
    Person(comercial, "Empleado comercial", "Vincula clientes, solicita préstamos, abre cuentas")
    Person(analista, "Analista interno", "Empleados, estados, aprueba/desembolsa, auditoría")

    System(bank, "Sistema Bancario", "API hexagonal Spring Boot: banca por roles, JWT, MySQL + MongoDB")
    System_Ext(spa, "Frontend SPA", "Vite/React en FRONTEND_ORIGIN, consume la API")
    SystemDb(mysql, "MySQL bank_db", "Estado relacional: usuarios, clientes, cuentas, préstamos, transferencias")
    SystemDb(mongo, "MongoDB audit_db", "Colección inmutable audit_logs")

    Rel(publico, bank, "POST /api/v1/auth/**")
    Rel(natural, bank, "GET/POST/PUT /api/v1/natural-customer/**")
    Rel(biz, bank, "/api/v1/business-customer/**")
    Rel(operador, bank, "/api/v1/business-operator/**")
    Rel(supervisor, bank, "/api/v1/business-supervisor/**")
    Rel(teller, bank, "/api/v1/teller/**")
    Rel(comercial, bank, "/api/v1/commercial/**")
    Rel(analista, bank, "/api/v1/internal-analyst/**")
    Rel(spa, bank, "HTTPS/JSON + Authorization: Bearer")
    Rel(bank, mysql, "JPA/Hibernate, DDL automático")
    Rel(bank, mongo, "Spring Data Mongo, audit_logs")
```

## Actores y endpoints (resumen)

| Actor | Prefijo | Autenticación | Operaciones |
|---|---|---|---|
| Público | `/api/v1/auth` | No (login/registro) + logout con JWT | `POST /login`, `POST /logout`, `POST /register/natural-customer`, `POST /register/business-customer`, `POST /register/user` |
| Cliente natural | `/api/v1/natural-customer` | `ROLE_NATURAL_CUSTOMER` | Perfil (GET/PUT), cuentas, balance, productos, préstamos (solicitar, consultar, pagar), transferencias, operaciones |
| Cliente empresarial | `/api/v1/business-customer` | `ROLE_BUSINESS_CUSTOMER` | Perfil, productos, cuentas, préstamos, `POST /users` (delega operador/supervisor), aprueba/rechaza transferencias propias |
| Operador | `/api/v1/business-operator` | `ROLE_BUSINESS_OPERATOR` | Cuentas, `POST /transfers` (202, umbral), `POST /transfers/{id}/submit`, operaciones |
| Supervisor | `/api/v1/business-supervisor` | `ROLE_BUSINESS_SUPERVISOR` | `GET /transfers/pending`, aprueba/rechaza, operaciones |
| Cajero | `/api/v1/teller` | `ROLE_TELLER_EMPLOYEE` | Cliente por identificación, cuentas (abrir, consultar, balance), depósitos, retiros, bloquear/desbloquear/cerrar |
| Comercial | `/api/v1/commercial` | `ROLE_COMMERCIAL_EMPLOYEE` | Clientes (consultar, actualizar PUT, productos), préstamos (solicitar, estado), abrir cuentas |
| Analista | `/api/v1/internal-analyst` | `ROLE_INTERNAL_ANALYST` | `POST /users/employee`, estado de clientes, aprobar/rechazar/desembolsar/cerrar (DELETE) préstamos, `GET /audit-logs`, operaciones, estado de usuarios |

Detalle método por método: `11-endpoints-flujos.md`.
