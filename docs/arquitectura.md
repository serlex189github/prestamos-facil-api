# Arquitectura del sistema

El proyecto utiliza Arquitectura Hexagonal para mantener separadas las reglas de
negocio de los detalles técnicos como Spring, PostgreSQL, JPA y los controladores
REST.

```mermaid
flowchart TD

    CLIENTE[Cliente / Postman / Swagger]

    subgraph ENTRADA[Adaptadores de entrada]
        CONTROLLERS[Controllers REST]
        DTOREST[DTOs y Mappers REST]
    end

    subgraph APLICACION[Application]
        PUERTOSIN[Puertos de entrada]
        USECASES[Casos de uso]
        PUERTOSOUT[Puertos de salida]
    end

    subgraph DOMINIO[Domain]
        MODELOS[Modelos de dominio]
        ENUMS[Estados y decisiones]
        REGLAS[Reglas de negocio]
    end

    subgraph SALIDA[Adaptadores de salida]
        JPA[Persistencia JPA]
        JDBC[JdbcTemplate]
        NOTIFICACION[Notificación simulada]
    end

    subgraph DATOS[Infraestructura externa]
        POSTGRES[(PostgreSQL)]
        SP[Procedimiento almacenado]
    end

    CLIENTE --> CONTROLLERS
    CONTROLLERS --> DTOREST
    DTOREST --> PUERTOSIN
    PUERTOSIN --> USECASES
    USECASES --> MODELOS
    USECASES --> ENUMS
    USECASES --> REGLAS
    USECASES --> PUERTOSOUT

    PUERTOSOUT --> JPA
    PUERTOSOUT --> JDBC
    PUERTOSOUT --> NOTIFICACION

    JPA --> POSTGRES
    JDBC --> SP
    SP --> POSTGRES
```
