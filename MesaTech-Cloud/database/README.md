# Base de datos local

MesaTech Cloud usa PostgreSQL 16 para la persistencia local.

## Requisitos

- PostgreSQL 16+
- Base de datos: `mesatech`
- Usuario: `mesatech`
- Contraseña: `mesatech`

## Ejecutar con Docker

```bash
docker compose -f database/docker-compose.yml up -d
```

## Conexión local

- Host: `localhost`
- Puerto: `5432`
- Base: `mesatech`
- Usuario: `mesatech`
- Contraseña: `mesatech`

## Script de inicialización

Se ejecuta desde `database/scripts/init.sql` y crea:

- esquema inicial para solicitudes
- esquema inicial para catálogo
- usuarios de prueba locales
- categorías y prioridades iniciales
- solicitudes demo

## Preparación para AWS

Cuando el servicio pase a AWS, la base de datos se moverá a RDS PostgreSQL y las variables de entorno se actualizarán para apuntar al endpoint del RDS sin cambiar la lógica de negocio.
