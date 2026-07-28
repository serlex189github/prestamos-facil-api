# Préstamos Fácil API

API REST para gestionar el ciclo de vida de solicitudes de préstamo, desde el
registro de usuarios y solicitudes hasta su evaluación, formalización, generación
del plan de pagos y consulta del monto total aprobado.

El proyecto fue desarrollado como prueba técnica utilizando Java 21, Spring Boot,
PostgreSQL y Arquitectura Hexagonal.

---

## Funcionalidades

- Registro de usuarios.
- Registro de solicitudes de préstamo.
- Consulta paginada de solicitudes.
- Filtro de solicitudes por estado.
- Evaluación manual de solicitudes.
- Evaluación automática mediante procedimiento almacenado.
- Aprobación y rechazo de solicitudes.
- Formalización del préstamo aprobado.
- Generación del plan de pagos.
- Simulación de notificaciones.
- Reporte del monto acumulado de préstamos aprobados.
- Documentación mediante Swagger/OpenAPI.
- Migraciones de base de datos con Flyway.
- Validaciones de entrada y manejo global de excepciones.
- Pruebas unitarias, de integración y de arquitectura.

---

## Tecnologías

- Java 21
- Spring Boot 3.5.16
- Maven
- PostgreSQL
- Spring Data JPA
- JDBC Template
- Flyway
- Lombok
- Bean Validation
- Swagger / OpenAPI
- Docker y Docker Compose
- JUnit 5
- Mockito
- Testcontainers
- ArchUnit

---

## Arquitectura

El proyecto utiliza Arquitectura Hexagonal, separando el dominio y los casos de
uso de las tecnologías externas.

```text
┌──────────────────────────────────────────────┐
│              Adaptadores de entrada          │
│         Controllers REST / Swagger           │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│                 Application                  │
│       Puertos de entrada y casos de uso      │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│                    Domain                    │
│       Modelos, enumeraciones y reglas         │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│              Adaptadores de salida           │
│ JPA, JDBC, PostgreSQL y notificaciones       │
└──────────────────────────────────────────────┘
```

### Estructura principal

```text
src/main/java/com/prestamosfacil
├── application
│   ├── dto
│   ├── port
│   │   ├── in
│   │   └── out
│   └── usecase
├── domain
│   ├── enums
│   ├── exception
│   └── model
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   └── rest
    │   └── out
    │       ├── notification
    │       └── persistence
    └── config
```

### Responsabilidades

- `domain`: modelos y reglas de negocio sin dependencia de frameworks.
- `application`: casos de uso y definición de puertos.
- `infrastructure`: controladores REST, persistencia, notificaciones y configuración.
- `adapter.in`: mecanismos que invocan la aplicación.
- `adapter.out`: implementaciones de persistencia y servicios externos.

---

## Requisitos

Para ejecutar el proyecto se requiere:

- JDK 21
- Docker
- Docker Compose
- Git

El repositorio incluye Maven Wrapper, por lo que no es necesario instalar Maven
globalmente.

---

## Clonar el repositorio

```bash
git clone https://github.com/serlex189github/prestamos-facil-api.git
cd prestamos-facil-api
```

---

## Base de datos

La aplicación utiliza PostgreSQL.

Configuración predeterminada:

| Propiedad | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `55432` |
| Base de datos | `prestamos_facil` |
| Usuario | `prestamos` |
| Contraseña | `prestamos` |

La configuración puede modificarse mediante variables de entorno:

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `SERVER_PORT` | Puerto de la aplicación |

---

## Ejecución local

### 1. Levantar PostgreSQL

```bash
docker compose up -d postgres
```

Comprobar el estado de los contenedores:

```bash
docker compose ps
```

### 2. Ejecutar la aplicación

En Git Bash o Linux:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La API quedará disponible en:

```text
http://localhost:8080
```

---

## Flyway

Las migraciones se encuentran en:

```text
src/main/resources/db/migration
```

Flyway ejecuta automáticamente las migraciones al iniciar la aplicación.

Hibernate utiliza:

```yaml
ddl-auto: validate
```

Por lo tanto, Hibernate valida el esquema, pero no lo crea ni modifica
automáticamente.

---

## Documentación de la API

### Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

```text
http://localhost:8080/v3/api-docs
```

### Actuator Health

```text
http://localhost:8080/actuator/health
```

---

## Endpoints

### Usuarios

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/usuarios` | Registrar un usuario |

### Solicitudes de préstamo

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/solicitudes` | Registrar una solicitud |
| `GET` | `/api/v1/solicitudes` | Consultar solicitudes |
| `PATCH` | `/api/v1/solicitudes/{solicitudId}/evaluacion-manual` | Evaluar manualmente |
| `POST` | `/api/v1/solicitudes/{solicitudId}/evaluacion-automatica` | Evaluar automáticamente |

### Reportes

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/reportes/prestamos-aprobados` | Consultar préstamos aprobados |

---

## Consulta paginada

El endpoint permite filtrar por estado y configurar la paginación:

```http
GET /api/v1/solicitudes?estado=PENDIENTE_REVISION&page=0&size=10
```

Parámetros:

| Parámetro | Obligatorio | Valor predeterminado |
|---|---:|---:|
| `estado` | No | Sin filtro |
| `page` | No | `0` |
| `size` | No | `10` |

---

## Estados de una solicitud

Las solicitudes pueden pasar por los siguientes estados:

```text
PENDIENTE_REVISION
REVISION_MANUAL
APROBADA
RECHAZADA
```

Las transiciones son controladas por los casos de uso para evitar modificaciones
inválidas o evaluaciones repetidas.

---

## Evaluación manual

Permite aprobar o rechazar una solicitud mediante:

```http
PATCH /api/v1/solicitudes/{solicitudId}/evaluacion-manual
```

Cuando la decisión es aprobatoria:

1. Se valida la solicitud.
2. Se valida el tipo de préstamo.
3. Se formaliza el préstamo.
4. Se genera el plan de pagos.
5. Se actualiza la solicitud.
6. Se envía una notificación simulada.

---

## Evaluación automática

La evaluación automática se ejecuta mediante:

```http
POST /api/v1/solicitudes/{solicitudId}/evaluacion-automatica
```

El proceso consulta un procedimiento almacenado en PostgreSQL que evalúa reglas
como:

- Capacidad máxima de endeudamiento.
- Relación entre ingresos y obligaciones actuales.
- Valor de la nueva cuota.
- Condiciones para aprobación, rechazo o revisión manual.

El resultado puede ser:

```text
APROBADA
REVISION_MANUAL
RECHAZADA
```

Cuando la solicitud es aprobada, se reutiliza el mismo proceso de formalización
empleado por la evaluación manual.

---

## Cálculo financiero

La cuota mensual utiliza el sistema de cuota fija:

```text
Cuota = P × [i × (1 + i)^n] / [(1 + i)^n - 1]
```

Donde:

- `P`: capital solicitado.
- `i`: tasa efectiva mensual.
- `n`: número de cuotas.

Para cada periodo se calcula:

```text
Interés       = saldo × tasa mensual
Abono capital = cuota - interés
Nuevo saldo   = saldo - abono capital
```

Los cálculos utilizan `BigDecimal` para conservar precisión monetaria y controlar
el redondeo.

---

## Reporte de préstamos aprobados

El reporte puede consultarse mediante:

```http
GET /api/v1/reportes/prestamos-aprobados
```

El endpoint retorna información consolidada de los préstamos aprobados, incluido
el monto total acumulado.

---

## Manejo de errores

La API implementa manejo global de excepciones para responder de manera
consistente ante situaciones como:

- Datos inválidos.
- Usuario duplicado.
- Recurso no encontrado.
- Tipo de préstamo inactivo.
- Solicitud en estado no permitido.
- Decisión manual inválida.
- Errores de validación.
- Reglas de negocio incumplidas.

---

## Pruebas

El proyecto cuenta con:

- Pruebas unitarias de dominio.
- Pruebas de casos de uso.
- Pruebas de controladores.
- Pruebas de persistencia.
- Pruebas de integración con PostgreSQL.
- Pruebas de migraciones.
- Pruebas de rollback transaccional.
- Pruebas de arquitectura con ArchUnit.
- Testcontainers para ejecutar PostgreSQL real durante las pruebas.

Ejecutar todas las pruebas:

```bash
./mvnw clean test
```

Resultado actual:

```text
Tests run: 82, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

También puede ejecutarse la verificación completa:

```bash
./mvnw clean verify
```

Docker debe estar activo para las pruebas que utilizan Testcontainers.

---

## Decisiones técnicas

### Arquitectura Hexagonal

Se utilizó para mantener el dominio y los casos de uso independientes de Spring,
JPA, PostgreSQL y otros detalles externos.

### Flyway

La estructura de la base de datos y los procedimientos almacenados se versionan
mediante migraciones reproducibles.

### Testcontainers

Las pruebas de integración utilizan una instancia real de PostgreSQL, evitando
diferencias entre una base de datos en memoria y el motor utilizado en producción.

### ArchUnit

Las reglas de arquitectura se validan automáticamente para evitar dependencias
indebidas entre capas.

### Formalización centralizada

La creación del préstamo y del plan de pagos se concentra en un servicio de
formalización reutilizado por la evaluación manual y automática.

### BigDecimal

Todos los valores financieros se procesan con `BigDecimal` para evitar errores
de precisión asociados con `double` o `float`.

---

## Autor

**Sergio Alexander Gómez Peña**

Backend Java Developer
