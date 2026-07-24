# Préstamos Fácil API

API REST para registrar usuarios, gestionar solicitudes de préstamo, procesar
aprobaciones manuales y automáticas, generar planes de pago y consultar el
monto total aprobado.

## Stack

- Java 21
- Spring Boot 3.5.16
- Maven
- PostgreSQL 17
- Spring Data JPA
- Flyway
- Swagger/OpenAPI
- JUnit 5, ArchUnit y Testcontainers

## Estado

El repositorio contiene el proyecto base y la estructura inicial de arquitectura
hexagonal. Las funcionalidades se implementarán de forma incremental.

## Requisitos locales

- JDK 21
- Docker con Docker Compose

El proyecto incluye Maven Wrapper, por lo que no requiere una instalación global
de Maven.

## Ejecución inicial

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080` y Swagger UI en
`http://localhost:8080/swagger-ui.html`.

## Verificación

```bash
./mvnw clean verify
```

## Arquitectura

```text
domain          -> modelos y reglas de negocio puras
application     -> puertos y casos de uso
infrastructure  -> REST, PostgreSQL, notificaciones y configuración
```

Las decisiones funcionales y arquitectónicas se ampliarán conforme avance la
implementación.

