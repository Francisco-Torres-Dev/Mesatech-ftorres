# MesaTech Cloud

MesaTech Cloud es una plataforma local para la gestión de solicitudes de soporte tecnológico, diseñada para funcionar en un entorno Cloud Native moderno con una arquitectura preparada para migrar a Microsoft Entra ID y AWS API Gateway sin reescribir la lógica de negocio.

## Objetivo

Permitir que:

- clientes creen y consulten sus solicitudes
- operadores revisen y actualicen estados
- administradores gestionen catálogo y visualicen todo el sistema

## Arquitectura local

React → BFF Spring Boot → Microservicios Spring Boot → PostgreSQL

## Arquitectura futura

React → Microsoft Entra ID → AWS API Gateway → BFF → Microservicios → RDS PostgreSQL

## Estructura

- frontend
- bff
- ms-solicitudes
- ms-catalogo
- database
- docs

## Requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16+
- Docker (opcional, recomendado para PostgreSQL local)

## Configuración de PostgreSQL

Usar la base `mesatech` en `localhost:5432` con usuario y contraseña `mesatech`.

## Variables de entorno

Copiar `.env.example` a `.env` si se quiere usar configuración de entorno local.

## Ejecutar localmente

### 1. Base de datos

```bash
docker compose -f database/docker-compose.yml up -d
```

### 2. Microservicio Catálogo

```bash
cd MesaTech-Cloud/ms-catalogo
mvn spring-boot:run
```

### 3. Microservicio Solicitudes

```bash
cd MesaTech-Cloud/ms-solicitudes
mvn spring-boot:run
```

### 4. BFF

```bash
cd MesaTech-Cloud/bff
mvn spring-boot:run
```

### 5. Frontend

```bash
cd MesaTech-Cloud/frontend
npm install
npm run dev -- --host 0.0.0.0
```

Abrir en `http://localhost:5173`

## Usuarios de prueba

- Cliente: `cliente@mesatech.local`
- Operador: `operador@mesatech.local`
- Administrador: `admin@mesatech.local`

Rol local seleccionable desde el login.

## Endpoints principales

- `POST /v1/auth/local/login`
- `POST /v1/solicitudes`
- `GET /v1/solicitudes/mias`
- `GET /v1/solicitudes`
- `PUT /v1/solicitudes/{id}/estado`
- `GET /v1/catalogo`
- `POST /v1/catalogo/categorias`
- `PUT /v1/catalogo/categorias/{id}`
- `DELETE /v1/catalogo/categorias/{id}`
- `GET /v2/solicitudes/{id}`

## Versionado

- `v1` permanece disponible y estable.
- `v2` ofrece detalles extendidos sin romper `v1`.

## Autenticación local

La autenticación local no usa passwords ni secretos. El frontend selecciona el rol para simular al usuario y el BFF valida un token local generado en memoria.

## Preparación futura para Entra ID y AWS

El código está preparado para cambiar únicamente la URL base y la lógica de autenticación, sin reescribir la lógica de negocio. Los puntos pendientes son la configuración real de Microsoft Entra ID y la integración con API Gateway/AWS.

## Licencia

Proyecto académico/demo.
