# PROMPT DE EVALUACIÓN AGÉNTICA: ETAPA 1 - MODELADO DDD EN JAVA

Este documento contiene las instrucciones detalladas, la rúbrica cuantitativa y la guía de ejecución para la evaluación automatizada y agéntica de repositorios estudiantiles. El agente evaluador debe ejecutar este flujo iterando sobre cada subdirectorio del espacio de trabajo.

---

## 1. IDENTIFICACIÓN Y CONTEXTO DE EVALUACIÓN

### 1.1. Identidad del Estudiante
- **Nombre del Estudiante:** Se asume directamente que **el nombre de la subcarpeta raíz** evaluada corresponde al nombre e identidad del estudiante (ej. `/Juan_Perez/` -> Estudiante: Juan Perez).

### 1.2. Dominio y Contexto Base (`especificacion_funcional_nexusmarket.md`)
Cada repositorio debe ser contrastado contra los requerimientos del dominio del Marketplace NexusMarket definidos en la especificación funcional (`especificacion_funcional_nexusmarket.md`):
- **Entidades del Dominio:** `User` (Comprador, Vendedor, Operador Logístico, Administrador, Supervisor), `Buyer`, `Seller`, `Warehouse` (Bodega), `Product` (Catálogo, Productos Físicos/Digitales, Variantes), `Inventory` (Inventario Distribuido por Bodega), `Cart` / `CartItem`, `Order` (Pedido), `Invoice` (Factura), `Shipment` (Envío/Logística), `Return` / `Refund` (Devoluciones y Reembolsos).
- **Estados y Tipos Acotados (Enums / Value Objects):** Estado de Usuario (`UserStatus`: Active, Blocked), Rol de Usuario (`UserRole`: Buyer, Seller, LogisticOperator, Admin, Supervisor), Tipo de Producto (`ProductType`: Physical, Digital), Estado de Producto (`ProductStatus`: Published, Suspended, Discontinued), Tipo de Movimiento de Inventario (`MovementType`: Inflow, Reservation, Sale, Adjustment, Return), Estado de Pedido (`OrderStatus`: Cart, PendingPayment, Paid, Shipped, Delivered), Estado Comercial del Comprador, etc.
- **Validaciones y Reglas:** Identificación y correo únicos por usuario, un único rol por usuario, prohibición estricta de existencias negativas de inventario, inmutabilidad de pedidos finalizados, registro de vendedores únicamente por el Administrador.
- **Estructura de Arquitectura Hexagonal y Adapters Requeridos:**
  - **Puertos de Entrada por Rol (`domain/ports/in/`):** Organizados exclusivamente por rol de usuario (`PublicAccessPort`, `NaturalCustomerPort`, `BusinessCustomerPort`, `BusinessOperatorPort`, `BusinessSupervisorPort`, `TellerEmployeePort`, `CommercialEmployeePort`, `InternalAnalystPort`).
  - **Casos de Uso (`adapters/useCases/`):** Clases concretas que implementan los Puertos de Entrada por Rol e **inyectan las clases de Servicio de Dominio** (`domain/services/`), las cuales contienen la lógica de negocio pura e inyectan los Puertos de Salida.
  - **Adaptadores REST (`adapters/rest/`):** Controladores y DTOs (`RequestDTO` / `ResponseDTO`). El endpoint de `login` retorna un JWT con el rol y datos suficientes para reconstruir el objeto de dominio `User`. Las clases REST evalúan el rol del token con respecto al caso de uso (Input Port) e inyectan al caso de uso el objeto `User` reconstruido a partir del JWT.
  - **Adaptadores de Persistencia (`adapters/persistence/`):** Cada adaptador de salida desacopla el dominio definiendo sus propias **Entities/Documents (Repository DTOs)**, **Mappers** (`Domain` ↔ `Entity/Document`) y **Repositorios** según el stack tecnológico:
    - **Java Stack:** Spring Data JPA (`@Entity`) para relacional, Spring Data MongoDB (`@Document`) para NoSQL.
    - **TypeScript Stack:** TypeORM / Prisma (`@Entity()`) para relacional, Mongoose (`Schema`/`Document`) para NoSQL.

---

## 2. INSTRUCCIONES DE CONTROL DE FLUJO Y SESIONES AGÉNTICAS

### 2.1. Gestión de Sesión Aislada por Estudiante
Para garantizar evaluaciones objetivas y evitar contaminación cruzada de contexto entre estudiantes:
1. **Inicio de Sesión:** El agente debe iniciar un ciclo de análisis aislado por cada subcarpeta de estudiante `{nombre_estudiante}`.
2. **Carga de Contexto Aislada:** Cargar en memoria únicamente:
   - La especificación funcional base del problema NexusMarket (`especificacion_funcional_nexusmarket.md`).
   - La especificación documental del estudiante (`{nombre_estudiante}/SDD/`).
   - El código fuente Java del estudiante (`{nombre_estudiante}/src/` o `{nombre_estudiante}/nexusmarket/src/`).
3. **Cierre de Sesión:** Al finalizar la calificación del estudiante, guardar el archivo `{nombre_estudiante}/evaluacion1.md` y liberar el contexto antes de iniciar el análisis de la siguiente carpeta.

### 2.2. Verificación de Idempotencia (Omisión de Repositorios)
Antes de procesar la subcarpeta `{nombre_estudiante}`:
1. Validar si existe el archivo `evaluacion1.md` o `evaluacion1` en la raíz de la carpeta del estudiante.
2. **SI EL ARCHIVO EXISTE:** Omitir inmediatamente la evaluación de esa carpeta y pasar al siguiente repositorio.
3. **SI EL ARCHIVO NO EXISTE:** Continuar con la evaluación.

### 2.3. Regla Descalificante de Lenguaje de Programación
El proyecto debe estar implementado **obligatoriamente en Java**.
- Inspeccionar el directorio de código fuente (`src/` o `bank/src/`).
- Si **NO** se encuentran archivos de código fuente con extensión `.java`, el proyecto se descalifica con **Nota Final: 0.0 / 5.0**.
- Se debe generar de inmediato el archivo `evaluacion1.md` en el directorio del estudiante especificando la descalificación por lenguaje y continuar con el siguiente repositorio.

### 2.4. Prevención de Alucinaciones y Trazabilidad
- **Trazabilidad estricta:** Cada calificación y penalización debe estar respaldada por evidencias observables (rutas de archivo exactas, clases, métodos o atributos). No asumir la existencia de elementos no observables.
- **Excepciones permitidas:** No penalizar el uso de Lombok (`@Data`, `@Getter`, etc.) ni de `@Service` por sí mismos, salvo que incurran en violaciones explícitas de la arquitectura del dominio.

---

## 3. ESCALA Y RÚBRICA DE EVALUACIÓN (0.0 a 5.0)

La calificación global se calcula según la siguiente fórmula:

```
Nota Final = (Criterio 1 * 0.20) + (Criterio 2 * 0.30) + (Criterio 3 * 0.25) + (Criterio 4 * 0.15) + (Criterio 5 * 0.10)
```

| Criterio | Peso | Descripción | Excelente (5.0) | Aceptable (3.0 - 3.9) | Insuficiente (1.0 - 2.9) | Pésimo (0.0) |
| :--- | :---: | :--- | :--- | :--- | :--- | :--- |
| **1. Especificación en SDD** | **20%** | Existencia y calidad de la documentación de modelos en archivos `.md` o `.txt` dentro de la carpeta `{nombre_estudiante}/SDD/`. | Especificación detallada y clara en `SDD/` respondiendo a los requerimientos de `especificacion_funcional_nexusmarket.md`. | Especificación presente en `SDD/`, pero con falta de detalle en algunas reglas o atributos del negocio del Marketplace. | Documentación en `SDD/` muy escasa, confusa o incompleta. | No existe la carpeta `SDD/` o los archivos están vacíos. |
| **2. Modelado de Entidades en Java** | **30%** | Definición de clases de entidad de dominio en Java (`User`, `Product`, `Warehouse`, `Order`, `Inventory`, etc.) con identidades y comportamiento. | Entidades bien delimitadas en Java, representando el dominio de NexusMarket con identidades únicas y comportamiento DDD. | Entidades creadas en Java, pero con mezcla de responsabilidades o lógica acoplada a persistencia/infraestructura. | Clases anémicas (solo getters/setters sin comportamiento) o incompletas respecto al dominio. | Ausencia de clases de dominio o desorganización total. |
| **3. Value Objects vs. Primitivos** | **25%** | Reemplazo de `String` abiertos por `Enums` o `Value Objects` para valores acotados/validados (`OrderStatus`, `UserRole`, `ProductType`, etc.). | Uso consistente de `Enums` o `Value Objects` para estados y tipos del Marketplace. **Cero `String` abiertos** en campos acotados. | Uso de `Enums` en la mayoría de estados, pero persisten 1 o 2 atributos clave como `String` abierto. | Uso masivo de `String` o tipos primitivos para estados, roles y tipos (obsesión por primitivos). | No se utilizan `Enums` ni `Value Objects` en ninguna parte del modelo. |
| **4. Idioma y Convenciones (Java)** | **15%** | Código Java 100% en inglés (nombres de clases, atributos, métodos y paquetes). La documentación en `SDD/` puede estar en español. | Todo el código Java está escrito 100% en inglés con naming consistente y buenas prácticas Java. | Código en Java mayoritariamente en inglés, con spanglish o inconsistencias menores. | Mezcla frecuente de español e inglés en clases, atributos o métodos en Java. | Código Java escrito completamente en español. |
| **5. Correlación SDD vs. Código Java** | **10%** | Trazabilidad y coincidencia 1:1 entre los modelos descritos en `SDD/` y las clases creadas en Java. | Trazabilidad exacta 1:1 entre los modelos especificados en `SDD/` y las clases Java del estudiante. | Diferencias menores entre nombres o atributos especificados en `SDD/` y los implementados en Java. | Poca correspondencia entre la documentación de `SDD/` y el código en Java. | El modelo documentado en `SDD/` no guarda relación con el código en Java. |

---

## 4. PLANTILLA PARA REPORTE INDIVIDUAL (`evaluacion1.md`)

Para cada repositorio evaluado, guardar la retroalimentación dentro de la carpeta del estudiante `{nombre_estudiante}/evaluacion1.md` utilizando el siguiente formato:

```markdown
# Reporte de Evaluación - Etapa 1: Modelado DDD

**Estudiante / Repositorio:** [Nombre de la carpeta del estudiante]
**Fecha de Evaluación:** [AAAA-MM-DD]
**Nota Final:** **[Nota global calculada] / 5.0**

---

## Detalle por Criterios de Evaluación

### 1. Especificación en SDD (20%) - Puntaje: [0.0 - 5.0]
- **Estado:** [Excelente / Aceptable / Insuficiente / Pésimo]
- **Observaciones y Evidencias:**
  - Archivos analizados: [Citar rutas exactas, ej. SDD/Domain/Domain Model.md]
  - Hallazgos respecto a `especificacion_funcional_nexusmarket.md`: [Detallar calidad del contenido y modelo documentado]

### 2. Modelado de Entidades en Java (30%) - Puntaje: [0.0 - 5.0]
- **Estado:** [Excelente / Aceptable / Insuficiente / Pésimo]
- **Observaciones y Evidencias:**
  - Clases analizadas: [Citar clases, ej. src/main/java/domain/models/Product.java]
  - Hallazgos: [Detallar encapsulamiento, reglas de negocio e identidad de las entidades de NexusMarket]

### 3. Value Objects vs. Primitivos (25%) - Puntaje: [0.0 - 5.0]
- **Estado:** [Excelente / Aceptable / Insuficiente / Pésimo]
- **Observaciones y Evidencias:**
  - Enums / Value Objects encontrados: [Citar enums, ej. src/main/java/domain/enums/OrderStatus.java]
  - Penalizaciones por String abiertos: [Citar atributos en String si aplica]

### 4. Idioma y Convenciones en Java (15%) - Puntaje: [0.0 - 5.0]
- **Estado:** [Excelente / Aceptable / Insuficiente / Pésimo]
- **Observaciones y Evidencias:**
  - Evaluación de idioma: [Confirmar si el código Java está 100% en inglés o citar incoherencias]

### 5. Correlación SDD vs. Código Java (10%) - Puntaje: [0.0 - 5.0]
- **Estado:** [Excelente / Aceptable / Insuficiente / Pésimo]
- **Observaciones y Evidencias:**
  - Nivel de coincidencia: [Explicar consistencia entre la especificación en SDD y la implementación en Java]

---

## Comentarios Finales y Recomendaciones
- [Resumen cualitativo con puntos fuertes y aspectos a mejorar]
```

---

## 5. CONSOLIDACIÓN DE RESULTADOS FINALES (`notas_finales.md`)

Una vez completada la revisión de todas las subcarpetas de repositorios, generar o actualizar en la raíz del espacio de trabajo el archivo `notas_finales.md` con el siguiente contenido:

```markdown
# Reporte Consolidado de Evaluaciones - Etapa 1 DDD

**Fecha de Generación:** [AAAA-MM-DD]
**Total Repositorios Evaluados:** [Número de repositorios]

| Estudiante / Carpeta | Estado de Evaluación | Nota Final (0.0 - 5.0) |
| :--- | :--- | :---: |
| [Nombre_Estudiante_1] | Evaluado exitosamente | X.X |
| [Nombre_Estudiante_2] | Omitido (Ya evaluado previamente) | X.X |
| [Nombre_Estudiante_3] | Descalificado (No implementado en Java) | 0.0 |
```
