# Documentación C4 — Sistema Bancario

Modelo C4 en 4 niveles + una ficha por cada capa del proyecto + matriz de
flujos por endpoint. Los diagramas están en **Mermaid** (se renderizan en
GitHub / VS Code con la extensión Mermaid).

## Mapa de documentos

| # | Archivo | Contenido |
|---|---------|-----------|
| — | `README.md` | Este índice |
| L1 | `01-nivel-1-contexto.md` | C4 Nivel 1: contexto, actores (8 roles + público) y sistemas externos |
| L2 | `02-nivel-2-contenedores.md` | C4 Nivel 2: contenedores (`bank-app`, MySQL, MongoDB, frontend) y red |
| L3 | `03-nivel-3-componentes.md` | C4 Nivel 3: componentes internos y ciclo de vida de una petición |
| L4 | `04-nivel-4-codigo.md` | C4 Nivel 4: clases clave y colaboraciones a nivel de código |
| Capa | `05-capa-adapters-rest.md` | Adaptador de entrada REST: controllers, DTOs, mappers, errores |
| Capa | `06-capa-adapters-usecases.md` | Adaptadores de casos de uso (implementan puertos `in`) |
| Capa | `07-capa-adapters-persistence.md` | Adaptadores de persistencia JPA (MySQL) y MongoDB (auditoría) |
| Capa | `08-capa-adapters-security.md` | Adaptador de seguridad: JWT, filtro, `SecurityConfig`, BCrypt, principal |
| Capa | `09-capa-domain.md` | Dominio puro: modelos, value objects, servicios, puertos, excepciones |
| Capa | `10-capa-infrastructure.md` | Infraestructura: `DatabaseSeeder` y configuración residual |
| Flujos | `11-endpoints-flujos.md` | Matriz de ~50 endpoints + secuencias por grupo de rol |

## Correspondencia capas ↔ código

```text
bank/src/main/java/application/
├── adapters/rest/          → 05 (entrada HTTP)
├── adapters/useCases/      → 06 (implementan domain/ports/in)
├── adapters/persistence/   → 07 (implementan domain/ports/out contra MySQL/Mongo)
├── adapters/security/      → 08 (JWT, filtro, config, BCrypt, principal)
├── adapters/config/        → 08 §5 (DefaultBusinessConfigurationAdapter)
├── adapters/notification/  → 10 §4 (NoOpNotificationAdapter)
├── domain/                 → 09 (models, valueobjects, enums, services,
│                                 ports/in, ports/out, exceptions)
└── infrastructure/seed/    → 10 (DatabaseSeeder)
```

Regla transversal: **las dependencias apuntan al dominio**.
`rest → ports/in → useCases → services → ports/out → persistence/security`.
El dominio nunca importa Spring, JPA, Mongo, HTTP ni JSON.

## Convenciones de los diagramas

- `C4Context / C4Container / C4Component` para L1–L3 (sintaxis Mermaid C4).
- `sequenceDiagram` para el flujo de cada grupo de endpoints (L3 aplicado).
- `classDiagram` simplificado para L4 (solo clases y relaciones que participan
  en los flujos, no el 100 % del código).
