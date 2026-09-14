# Actividad: Funcionamiento de la Aplicación de Gestión de Información de un Banco

## Introducción y Objetivo del Proyecto

El presente enunciado define los requisitos funcionales y de negocio para el desarrollo de un sistema de información enfocado en la gestión de clientes, productos y operaciones clave de una entidad bancaria. El objetivo de este proyecto es que el estudiante diseñe e implemente una aplicación robusta, segura y escalable que cumpla con las normativas y flujos de trabajo descritos, infiriendo el modelo de datos (relacional y no relacional), las validaciones y los casos de uso a partir de la narrativa de negocio.

La aplicación servirá como el *core* transaccional y de gestión de la información fundamental del banco, permitiendo a distintos roles interactuar con los datos de clientes (personas naturales y empresas), cuentas, préstamos y transferencias, siempre bajo estrictas reglas de negocio y flujos de aprobación bien definidos.

# Descripción de los Roles

El sistema debe contemplar un modelo de acceso basado en roles, donde la visibilidad de la información y la capacidad de realizar operaciones están estrictamente limitadas por las responsabilidades de cada usuario dentro del banco o como cliente. A continuación, se detallan los roles principales y su interacción con la aplicación.

## Cliente Persona Natural

Este rol corresponde a los usuarios individuales del banco. Su interacción se centra en la consulta y operación sobre sus propios productos. Puede solicitar nuevos productos (como préstamos) y realizar transferencias.

| Campo | Descripción | Restricciones y Formato |
|---|---|---|
| Nombre completo | Nombre y apellidos de la persona. | Obligatorio. |
| Número de identificación | Cédula, DNI u otro identificador nacional. | **Único** en todo el aplicativo. |
| Correo electrónico | Dirección de contacto principal. | Obligatorio. Debe contener `@` y un dominio. |
| Número de teléfono | Teléfono de contacto. | Obligatorio. Longitud mínima de 7 dígitos y máxima de 15. |
| Fecha de nacimiento | Fecha de nacimiento de la persona. | Obligatorio. Formato DD/MM/YYYY. Debe ser mayor de edad (al menos 18 años). |
| Dirección | Domicilio registrado. | Obligatorio. |
| Rol | Cliente Persona Natural. | Valor fijo. |

**Visibilidad y Operaciones:** Solo puede visualizar el estado de sus cuentas, sus préstamos (detalles y pagos), y el historial de sus propias transferencias. Puede crear solicitudes de nuevos préstamos y transferencias entre cuentas propias o a terceros. **No puede** ver información de otros clientes ni cuentas empresariales.

## Cliente Empresa (Representante Legal / Usuario Administrador de Empresa)

Representa a la entidad legal que es cliente del banco. El representante legal (o el usuario administrador delegado) tiene la capacidad de gestionar los productos de la empresa.

**Visibilidad y Operaciones:** Puede visualizar todas las cuentas y préstamos asociados a la empresa. Puede delegar permisos a otros usuarios operativos de la empresa. Las transferencias de alto valor que cree el Empleado de Empresa podrían requerir su aprobación (ver Flujos de Aprobación).

| Campo | Descripción | Restricciones y Formato |
|---|---|---|
| Razón Social | Nombre legal de la empresa. | Obligatorio. |
| Número de identificación Fiscal (NIT) | Identificador tributario de la empresa. | **Único** en todo el aplicativo. |
| Correo electrónico | Correo de contacto corporativo. | Obligatorio. Debe contener `@` y un dominio. |
| Número de teléfono | Teléfono de contacto de la empresa. | Obligatorio. Longitud mínima de 7 dígitos y máxima de 15. |
| Dirección | Domicilio fiscal. | Obligatorio. |
| Representante Legal | Identificador del representante legal (referencia a Persona Natural). | Obligatorio. |
| Rol | Cliente Empresa. | Valor fijo. |

## Empleado de Ventanilla (Cajero / Asesor de Servicios)

Personal de contacto directo con el público. Sus operaciones son principalmente transaccionales y de soporte básico.

**Visibilidad y Operaciones:** Puede consultar el saldo y el estado de cualquier cuenta de cliente para realizar operaciones de caja (depósitos, retiros), siempre que el cliente se identifique correctamente. Puede registrar la apertura de nuevas cuentas para clientes existentes o nuevos. **No puede** ver información detallada de riesgos crediticios ni aprobar préstamos.

## Empleado Comercial (Asesor de Productos / Ejecutivo de Cuenta)

Personal dedicado a la venta de productos bancarios.

**Visibilidad y Operaciones:** Puede consultar información detallada de los clientes bajo su gestión, crear solicitudes de nuevos productos (préstamos, cuentas) en nombre del cliente. Puede realizar un seguimiento del estado de las solicitudes. **No puede** realizar aprobaciones de riesgo ni modificar saldos directamente.

## Empleado de Empresa (Usuario Operativo de Empresa)

Usuario autorizado por la empresa cliente para realizar operaciones diarias.

**Visibilidad y Operaciones:** Puede crear transferencias y pagos masivos (por ejemplo, nómina) desde las cuentas de la empresa. Solo ve los productos y operaciones de la empresa a la que está asociado. Las transferencias que superen un umbral predefinido deben pasar a estado "en espera de aprobación". **No tiene** capacidad de aprobación.

## Supervisor de Empresa (Usuario Aprobador / Administrador de Empresa)

Rol designado por la empresa para supervisar y autorizar operaciones críticas.

**Visibilidad y Operaciones:** Ve todas las operaciones y productos de su empresa. Su función principal es **aprobar o rechazar** las transferencias y pagos creados por el Empleado de Empresa que requieran autorización. También puede gestionar los usuarios operativos de la empresa.

## Analista Interno del Banco (Riesgo / Cumplimiento / Back-Office)

Personal especializado en la revisión y gestión de riesgos y normativas.

**Visibilidad y Operaciones:** Tiene acceso a la información de clientes, productos y operaciones para fines de análisis, cumplimiento y auditoría. Es el rol responsable de **aprobar o rechazar solicitudes de préstamos**. Puede consultar la bitácora completa de operaciones. **No puede** realizar operaciones transaccionales de ventanilla ni crear transferencias.

# Información de los Usuarios del Sistema

Todos los usuarios del sistema, sin importar su rol, deben registrar la siguiente información básica, la cual debe estar centralizada.

| Nombre del Campo | Descripción | Tipo de Dato | Longitud Máxima / Formato | Restricciones |
|---|---|---|---|---|
| `ID_Usuario` | Identificador único del usuario en el sistema. | Numérico (Entero) | N/A | **Clave principal**, único y obligatorio. |
| `ID_Relacionado` | Identificador de la Entidad Cliente (Persona Natural o Empresa) asociada. | Texto/Numérico | N/A | Obligatorio para todos los roles, excepto Analista Interno y Empleado. |
| `Nombre_Completo` | Nombre del usuario. | Texto | 100 | Obligatorio. |
| `ID_Identificacion` | Número de identificación (DNI, Cédula, NIT). | Texto | 20 | **Único** globalmente. Obligatorio. |
| `Correo_Electronico` | Email del usuario. | Texto | 100 | Obligatorio. Formato válido (`@`, dominio). |
| `Telefono` | Número de teléfono. | Texto | 15 | Obligatorio. Mínimo 7, máximo 15 dígitos. |
| `Fecha_Nacimiento` | Fecha de nacimiento. | Fecha | DD/MM/YYYY | Obligatorio para Personas Naturales. Validación: > 18 años. |
| `Direccion` | Dirección de residencia/contacto. | Texto | 200 | Obligatorio. |
| `Rol_Sistema` | Rol asignado en el aplicativo. | Texto (Catálogo) | 50 | Obligatorio. Valores: Cliente Persona Natural, Cliente Empresa, Empleado de Ventanilla, etc. |
| `Estado_Usuario` | Estado del usuario en el sistema. | Texto (Catálogo) | 15 | Obligatorio. Valores: Activo, Inactivo, Bloqueado. |

# Productos y Servicios Bancarios manejados por la aplicación

El sistema debe gestionar las siguientes entidades fundamentales del negocio, cuya información es estructurada y debe ser almacenada en una **Base de Datos Relacional (SQL)**.

## Cliente Persona Natural

Representa a la persona física que es titular o beneficiaria de productos. La estructura de datos es la ya definida en la tabla anterior.

## Cliente Empresa

Representa a la entidad jurídica. La estructura de datos es la ya definida en la tabla anterior, enfocada en los datos corporativos.

## Cuenta Bancaria

Representa los depósitos de dinero del cliente. Pueden ser de tipo 'Ahorros', 'Corriente', 'Personal' o 'Empresarial'.

| Nombre del Campo | Descripción | Tipo de Dato | Longitud Máxima / Formato | Restricciones |
|---|---|---|---|---|
| `Numero_Cuenta` | Identificador único de la cuenta bancaria. | Texto/Numérico | 20 | **Único**, obligatorio. |
| `Tipo_Cuenta` | Tipo específico de cuenta. | Texto (Catálogo) | 50 | Obligatorio. Valores: Ahorros, Corriente, Empresarial, etc. |
| `ID_Titular` | Referencia al `ID_Identificacion` del Cliente (Persona o Empresa). | Texto | 20 | Obligatorio. |
| `Saldo_Actual` | Monto de dinero disponible en la cuenta. | Numérico (Decimal) | N/A | Obligatorio. Debe ser mayor o igual a cero (salvo sobregiro autorizado). |
| `Moneda` | Moneda en la que opera la cuenta. | Texto (Catálogo) | 5 | Obligatorio. Valores: USD, COP, EUR, etc. |
| `Estado_Cuenta` | Estado operativo de la cuenta. | Texto (Catálogo) | 15 | Obligatorio. Valores: Activa, Bloqueada, Cancelada. |
| `Fecha_Apertura` | Fecha en que se creó la cuenta. | Fecha | DD/MM/YYYY | Obligatorio. |

## Préstamo / Crédito

Representa un producto de endeudamiento otorgado al cliente.

| Nombre del Campo | Descripción | Tipo de Dato | Longitud Máxima / Formato | Restricciones |
|---|---|---|---|---|
| `ID_Prestamo` | Identificador único del préstamo. | Numérico (Entero) | N/A | **Único**, obligatorio. |
| `Tipo_Prestamo` | Categoría del préstamo. | Texto (Catálogo) | 50 | Obligatorio. Valores: Consumo, Vehículo, Hipotecario, Empresarial. |
| `ID_Cliente_Solicitante` | Referencia al `ID_Identificacion` del Cliente. | Texto | 20 | Obligatorio. |
| `Monto_Solicitado` | Cantidad de dinero que el cliente pidió. | Numérico (Decimal) | N/A | Mayor que cero. Obligatorio. |
| `Monto_Aprobado` | Cantidad de dinero que el banco aprobó. | Numérico (Decimal) | N/A | Mayor que cero si el estado es `Aprobado` o `Desembolsado`. |
| `Tasa_Interes` | Tasa de interés anual del préstamo. | Numérico (Decimal) | N/A | Mayor que cero. Obligatorio. |
| `Plazo_Meses` | Duración del préstamo en meses. | Numérico (Entero) | N/A | Mayor que cero. Obligatorio. |
| `Estado_Prestamo` | Estado actual del proceso del préstamo. | Texto (Catálogo) | 20 | Obligatorio. Valores: En estudio, Aprobado, Rechazado, Desembolsado, En mora, Cancelado. |
| `Fecha_Aprobacion` | Fecha en que el Analista Interno aprobó. | Fecha | DD/MM/YYYY | Nulo si no está aprobado. |
| `Fecha_Desembolso` | Fecha en que se realizó el abono a la cuenta. | Fecha | DD/MM/YYYY | Nulo si no está desembolsado. |
| `Cuenta_Destino_Desembolso` | Número de cuenta donde se abona el dinero. | Texto/Numérico | 20 | Obligatorio si el estado es `Desembolsado`. Debe ser una cuenta Activa del cliente. |

## Transferencia

Representa el movimiento de fondos entre cuentas.

| Nombre del CampoDescripciónTipo de DatoLongitud Máxima / FormatoRestricciones |                                               |                    |                       |                                                                                                    |
| ----------------------------------------------------------------------------- | --------------------------------------------- | ------------------ | --------------------- | -------------------------------------------------------------------------------------------------- |
| ID\_Transferencia                                                             | Identificador único de la transferencia.      | Numérico (Entero)  | N/A                   | **Único**, Obligatorio.                                                                            |
| Cuenta\_Origen                                                                | Número de la cuenta de la que sale el dinero. | Texto/Numérico     | 20                    | Obligatorio. Debe ser una cuenta Activa.                                                           |
| Cuenta\_Destino                                                               | Número de la cuenta a la que llega el dinero. | Texto/Numérico     | 20                    | Obligatorio.                                                                                       |
| Monto                                                                         | Cantidad de dinero a transferir.              | Numérico (Decimal) | N/A                   | Mayor que cero. Obligatorio.                                                                       |
| Fecha\_Creacion                                                               | Fecha y hora en que se registró la solicitud. | Fecha/Hora         | DD/MM/YYYY HH\:MM\:SS | Obligatorio.                                                                                       |
| Fecha\_Aprobacion                                                             | Fecha y hora de la aprobación (si aplica).    | Fecha/Hora         | DD/MM/YYYY HH\:MM\:SS | Nulo si no requiere o no ha sido aprobada.                                                         |
| Estado\_Transferencia                                                         | Estado del proceso de transferencia.          | Texto (Catálogo)   | 30                    | Obligatorio. Valores: Pendiente, En espera de aprobación, Aprobada, Ejecutada, Rechazada, Vencida. |
| ID\_Usuario\_Creador                                                          | ID del usuario que generó la transferencia.   | Numérico (Entero)  | N/A                   | Obligatorio.                                                                                       |
| ID\_Usuario\_Aprobador                                                        | ID del usuario que aprobó (si aplica).        | Numérico (Entero)  | N/A                   | Nulo si no requiere o no ha sido aprobada.                                                         |

## Producto Bancario General (Catálogo)

Entidad que define los tipos de productos y servicios ofrecidos.

| Nombre del CampoDescripciónTipo de DatoLongitud Máxima / FormatoRestricciones |                                                                         |                  |     |                         |
| ----------------------------------------------------------------------------- | ----------------------------------------------------------------------- | ---------------- | --- | ----------------------- |
| Codigo\_Producto                                                              | Código único del producto/servicio.                                     | Texto            | 10  | **Único**, Obligatorio. |
| Nombre\_Producto                                                              | Nombre descriptivo del producto.                                        | Texto            | 100 | Obligatorio.            |
| Categoria                                                                     | Categoría (Cuentas, Préstamos, Servicios).                              | Texto (Catálogo) | 50  | Obligatorio.            |
| Requiere\_Aprobacion                                                          | Indica si la creación de este producto requiere un flujo de aprobación. | Booleano         | S/N | Obligatorio.            |

# Flujos de Aprobación de Operaciones

Ciertas operaciones críticas o de alto valor requieren una validación humana adicional para su ejecución, lo cual se gestiona mediante cambios de estado y la intervención de roles específicos.

**Principio General de Aprobación:** Toda operación que requiera aprobación debe registrar explícitamente: quién la creó, en qué momento, quién la aprobó o rechazó, en qué momento. Cada cambio de estado debe ser registrado en la Bitácora de Operaciones.

## Flujo de Aprobación de Préstamos

El proceso de un préstamo comienza con una solicitud y culmina con el desembolso o el rechazo.

1. **Creación de la Solicitud:** Un **Cliente Persona Natural**, un **Cliente Empresa** (a través de su administrador), o un **Empleado Comercial** puede ingresar una solicitud de préstamo.
2. **Estado Inicial:** La solicitud se registra con el `Estado_Prestamo` en **"En estudio"**.
3. **Aprobación/Rechazo:** El **Analista Interno del Banco** es el único rol con permiso para revisar y tomar la decisión final.
   - Si el Analista Interno aprueba, el estado cambia a **"Aprobado"**.
   - Si el Analista Interno rechaza, el estado cambia a **"Rechazado"**.
4. **Desembolso (Tras Aprobación):** Una vez en estado "Aprobado", un proceso interno (ejecutado por el Back-Office, representado por una acción del Analista Interno) debe marcar el préstamo como **"Desembolsado"** y:
   - Validar que el campo `Cuenta_Destino_Desembolso` esté definido y activo.
   - Aumentar el `Saldo_Actual` de la cuenta destino por el `Monto_Aprobado`.
   - Registrar el cambio de estado y el desembolso de fondos en la Bitácora de Operaciones.

## Flujo de Aprobación de Transferencias de Empresa (Alto Monto)

Las transferencias empresariales que superen un umbral de monto predefinido (Regla de Negocio) deben ser revisadas.

1. **Creación de la Solicitud:** Un **Empleado de Empresa** crea una transferencia o un lote de pagos desde una cuenta de la empresa.
2. **Estado Inicial:** Si la transferencia supera el umbral, el `Estado_Transferencia` es **"En espera de aprobación"**. Si no lo supera, se ejecuta directamente (pasa a estado "Ejecutada").
3. **Aprobación/Rechazo:** El **Supervisor de Empresa** (o el rol administrador de la empresa) es el único responsable de aprobar o rechazar estas transferencias.

- Si el Supervisor de Empresa aprueba:
  - Se valida la existencia de `Saldo_Actual` suficiente en la `Cuenta_Origen`.
  - Se ejecuta la transferencia (actualización de saldos de las cuentas involucradas).
  - El estado final cambia a **"Ejecutada"**.
  - Se registra la aprobación y la ejecución en la Bitácora de Operaciones.
- Si el Supervisor de Empresa rechaza, el estado final cambia a **"Rechazada"**.

4. **Condición de Vencimiento:** Si una transferencia permanece en estado **"En espera de aprobación"** por un período superior a **una hora** (60 minutos) desde su creación, el sistema debe cambiar su estado automáticamente a **"Vencida"**.

- No se realiza movimiento de fondos.
- Se debe registrar el evento de vencimiento en la Bitácora, indicando el motivo "vencida por falta de aprobación en el tiempo establecido".

# Bitácora de Operaciones (Almacenamiento NoSQL)

Para propósitos de auditoría, trazabilidad y cumplimiento, todas las operaciones significativas realizadas en el sistema deben ser registradas en un histórico inmutable, denominado **Bitácora de Operaciones**.

Debido a la variabilidad del contenido de los registros (una transferencia no tiene los mismos datos relevantes que la aprobación de un préstamo), la Bitácora debe ser diseñada para ser almacenada en una **Base de Datos No Relacional**, utilizando un modelo de documento o diccionario.

**Propósito:** La Bitácora se utiliza para el seguimiento histórico y la auditoría; **no se utiliza para calcular el** **`Saldo_Actual`** **de las cuentas**, el cual debe ser gestionado en la Base de Datos Relacional.

## Campos Mínimos del Registro de Bitácora

Cada documento de la Bitácora debe incluir los siguientes campos estructurados, además de un objeto o diccionario de "Datos\_Detalle" que contenga la información específica de la operación.

| Nombre del Campo | Descripción | Tipo de Dato | Restricciones |
|---|---|---|---|
| `ID_Bitacora` | Identificador único del registro de bitácora. | Texto/Numérico | Único, obligatorio. |
| `Tipo_Operacion` | Categoría de la operación realizada. | Texto | Obligatorio. Ej.: Apertura_Cuenta, Transferencia, Aprobacion_Prestamo, Desembolso, etc. |
| `Fecha_Hora_Operacion` | Momento exacto en que se generó el registro. | Fecha/Hora | Obligatorio. |
| `ID_Usuario` | Identificador del usuario que ejecutó la acción. | Numérico (Entero) | Obligatorio. |
| `Rol_Usuario` | Rol del usuario en el momento de la operación. | Texto | Obligatorio. |
| `ID_Producto_Afectado` | Referencia al producto principal de la operación (Cuenta, Préstamo, Transferencia). | Texto/Numérico | Obligatorio. |
| `Datos_Detalle` | Objeto/Documento con datos variables según `Tipo_Operacion`. | Documento (JSON/Diccionario) | Obligatorio. |

**Contenido de** **`Datos_Detalle`** **(Ejemplos):**

- **Para Transferencia Ejecutada:** Monto involucrado, Saldo\_Antes\_Origen, Saldo\_Despues\_Origen, Saldo\_Antes\_Destino, Saldo\_Despues\_Destino.
- **Para Aprobación de Préstamo:** Monto Aprobado, Tasa de Interés, Estado Anterior ('En estudio'), Nuevo Estado ('Aprobado'), ID del Analista Aprobador.
- **Para Vencimiento de Transferencia:** Motivo de vencimiento ("Falta de aprobación a tiempo"), Fecha y Hora de Vencimiento, ID del Usuario Creador.

# Reglas de Negocio y Condiciones de Validación

El sistema debe garantizar el cumplimiento de las siguientes reglas de negocio mediante validaciones en el código.

## Reglas Generales de Clientes y Productos

- **Unicidad de Identificación:** El `Número de identificación` (DNI/Cédula/NIT) debe ser único para cualquier cliente (Persona Natural o Empresa) en la base de datos.
- **Apertura de Cuentas:**

* No se puede abrir una cuenta a un cliente cuyo `Estado_Usuario` esté como 'Inactivo' o 'Bloqueado'.
* Cada `Cuenta Bancaria` debe tener un `Numero_Cuenta` único.
* El `Tipo_Cuenta` debe ser un valor válido del `Producto Bancario General` (catálogo).
* No se permiten operaciones (transferencias, retiros) en cuentas con `Estado_Cuenta` 'Bloqueada' o 'Cancelada', salvo procesos internos de cierre.

## Reglas de Préstamos / Créditos

- **Asociación de Clientes:** Todo préstamo debe estar asociado a un `ID_Cliente_Solicitante` que sea válido y activo.
- **Transiciones de Estado:**

* Un préstamo solo puede pasar de "En estudio" a "Aprobado" o "Rechazado".
* Solo el **Analista Interno** puede realizar la aprobación o rechazo.
* El paso a "Desembolsado" solo es posible desde el estado "Aprobado".

- **Validación de Desembolso:** No se puede marcar un préstamo como "Desembolsado" sin que se haya definido y validado la `Cuenta_Destino_Desembolso` (debe ser una cuenta activa del cliente).
- **Impacto Financiero del Desembolso:** Al realizar el desembolso:

* Se debe validar que el `Monto_Aprobado` sea mayor a cero.
* El `Saldo_Actual` de la cuenta destino debe aumentar en el `Monto_Aprobado`.
* Se debe generar un registro en la Bitácora.

## Reglas de Transferencias

- **Unicidad y Monto:** Toda transferencia debe tener un `ID_Transferencia` único y el `Monto` a transferir debe ser estrictamente mayor que cero.
- **Disponibilidad de Fondos:** No se permite ejecutar transferencias (pasar a estado "Ejecutada") desde `Cuenta_Origen` con `Saldo_Actual` insuficiente, a menos que exista una regla específica de sobregiro autorizado que deba ser validada.
- **Cuentas Operativas:** No se permiten transferencias desde `Cuenta_Origen` bloqueadas o canceladas.
- **Regla de Vencimiento de Aprobación:** Como se detalla en los flujos de aprobación, si una transferencia que requiere aprobación (empresa de alto monto) permanece en estado "En espera de aprobación" por más de una hora, debe cambiar automáticamente a **"Vencida"** y registrar el evento en la Bitácora.
- **Impacto Financiero de la Ejecución:** Toda transferencia ejecutada debe:

* Disminuir el `Saldo_Actual` de la `Cuenta_Origen`.
* Aumentar el `Saldo_Actual` de la `Cuenta_Destino` (si es interna).
* Registrar la operación en la Base de Datos Relacional y en la Bitácora NoSQL.

## Restricciones de Acceso por Rol (Seguridad de la Información)

Todos los usuarios cuentan con nombre de usuario y contraseña, para generar cualquier tipo de operacion deben estar debidamente logueados y tener permisos, en caso de tratar de acceder a un flujo al cual no tengan permisos se le debe impedir el desarrollo del mismo.

Las siguientes reglas definen quién puede ver qué información dentro de la aplicación.

- **Clientes (Persona Natural y Empresa):**

* Solo pueden consultar y operar sobre sus propios productos (`Cuenta Bancaria`, `Préstamo / Crédito`).
* Pueden ver el historial de sus propias operaciones registradas en la Bitácora, filtrado por su `ID_Producto_Afectado`.
* **No pueden** ver ninguna información o producto asociado a otros clientes.

- **Empleado de Ventanilla:**

* Puede consultar el `Saldo_Actual` y el `Estado_Cuenta` de cualquier cliente para propósitos de transacciones de caja.
* Puede registrar la apertura de nuevos productos de cuenta.
* **No puede** acceder a información de análisis de riesgo, datos crediticios detallados (como la Tasa de Interés) o la Bitácora completa de operaciones.

- **Empleado Comercial:**

* Puede acceder a la información completa de los clientes que tiene asignados (o que está gestionando para una solicitud).
* Puede consultar el estado de los préstamos en "En estudio" o "Rechazado" para dar seguimiento, pero **no puede** modificar su estado.
* **No puede** realizar operaciones que impacten saldos directamente, salvo la solicitud inicial de productos.

- **Empleado de Empresa (Operativo):**

* Acceso limitado estrictamente a las cuentas, préstamos y transferencias de la empresa a la que pertenece.
* Puede crear transferencias.

- **Supervisor de Empresa (Aprobador):**

* Acceso a la información de su empresa, con énfasis en las transferencias que están en estado **"En espera de aprobación"**.
* Es el único rol fuera del banco con capacidad de modificar el estado de las transferencias (Aprobar/Rechazar).

- **Analista Interno del Banco:**

* Tiene el acceso más amplio a la información de productos, clientes y operaciones.
* Es el rol primario para modificar el estado de los préstamos (Aprobar/Rechazar/Desembolsar).
* Tiene acceso completo de consulta a la **Bitácora de Operaciones**.
* **Crucialmente, no puede** modificar saldos de cuentas de forma arbitraria; solo puede hacerlo como resultado de un flujo de negocio definido (ej: desembolso de préstamo).

# Conclusiones para el Desarrollo

El desarrollo de esta **Aplicación de Gestión de Información de un Banco** requiere la comprensión profunda de los procesos de negocio y la segregación de la información.

El estudiante deberá deducir la estructura de la base de datos **Relacional (SQL)** para clientes, cuentas, préstamos y transferencias a partir de los campos y restricciones proporcionados. Adicionalmente, se debe diseñar el esquema de documentos para la **Bitácora de Operaciones (NoSQL)**, asegurando que capture toda la trazabilidad necesaria para auditoría.

El éxito del proyecto radicará en la correcta implementación de los **Flujos de Aprobación** y de las **Reglas de Negocio y Validaciones** en cada caso de uso, garantizando que el sistema sea consistente, seguro y que respete las restricciones de acceso por rol.