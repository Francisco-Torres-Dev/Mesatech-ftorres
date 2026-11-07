# Diagrama de Despliegue y Arquitectura Real de MesaTech Cloud (EP2)
**Integrantes:** Francisco Torres — Kevin Mesías  
**Asignatura:** Desarrollo Cloud Native I (DSY1107) — Evaluación Parcial 2

---

## 1. Diagrama de Arquitectura Completo (Mermaid)

```mermaid
flowchart TD
    subgraph CLIENTE["Capas de Cliente y Seguridad Perimetral"]
        SPA["Frontend SPA React (Vite)\n(Puerto 5173 / Localhost)"]
        ENTRA["Microsoft Entra ID (Azure AD)\n(OAuth2.0 / OpenID Connect)\nTenant: 2b28c657... | Client: 964cf08d..."]
        APIGW["AWS HTTP API Gateway (13fetz3wok)\n- JWT Authorizer (https://sts.windows.net/...)\n- CORS Habilitado (Origins: *)\n- Rutas: /v1/{proxy+} y /v2/{proxy+}"]
    end

    subgraph AWS_VPC["Amazon VPC (us-east-1) - Red 172.31.0.0/16"]
        subgraph EC2_A["EC2 A: MesaTech-EC2-A-Catalogo-BFF (i-05a7867f9d3c7ebaf)"]
            BFF["MesaTech BFF\nSpring Boot (Puerto 8080)\nIP Privada: 172.31.5.17\nIP Pública: 18.233.216.8"]
            CAT["ms-catalogo\nSpring Boot (Puerto 8081)\n[ROL: PUBLICADOR DE EVENTOS]"]
        end

        subgraph EC2_B["EC2 B: MesaTech-EC2-B-Solicitudes (i-0264b18bc3f0955a3)"]
            SOL["ms-solicitudes\nSpring Boot (Puerto 8082)\nIP Privada: 172.31.4.52\nIP Pública: 98.82.97.78\n[ROL: CONSUMIDOR DE EVENTOS]"]
        end

        subgraph EC2_C["EC2 C: MesaTech-EC2-C-RabbitMQ (i-0073a1baff203a9b4)"]
            BROKER["RabbitMQ 3.13-management (Docker)\nIP Privada: 172.31.13.15\nIP Pública: 34.236.12.11\nPuertos: 5672 (AMQP), 15672 (Web UI)\nVhost: 'mesatech'"]
            EXCHANGE["Exchange Direct (Durable)\n'catalogo.exchange'"]
            QUEUE["Queue (Durable)\n'solicitudes.catalogo.queue'"]
            BINDING["Binding: routing_key='categoria.creada'"]
        end

        subgraph RDS_DATABASES["Amazon RDS (Database-per-Service) - PostgreSQL 16.9"]
            RDS_CAT["Amazon RDS Catálogo\nmesatech-rds-catalogo\nBD: 'mesatech_catalogo'\n[PROPIETARIO EXCLUSIVO: ms-catalogo]"]
            RDS_SOL["Amazon RDS Solicitudes\nmesatech-rds-solicitudes\nBD: 'mesatech_solicitudes'\n[PROPIETARIO EXCLUSIVO: ms-solicitudes]"]
        end
    end

    %% Relaciones de Autenticación y API Gateway
    SPA -.->|"1. Inicia sesión (Popup / Redirect)"| ENTRA
    ENTRA -.->|"2. Emite Access Token JWT"| SPA
    SPA -->|"3. Peticiones HTTP con Bearer Token"| APIGW
    APIGW -->|"4. Valida JWT y reenvía HTTP Proxy"| BFF

    %% Relaciones del BFF hacia microservicios
    BFF -->|"HTTP REST (localhost:8081/v1/catalogo)"| CAT
    BFF -->|"HTTP REST privado (172.31.4.52:8082/v1/solicitudes)"| SOL

    %% Publicación y Consumo AMQP
    CAT -->|"5. Publica Evento AMQP (Publisher: publicador_catalogo)"| EXCHANGE
    EXCHANGE -->|"6. Enruta mediante routing_key 'categoria.creada'"| QUEUE
    QUEUE -->|"7. Consume Evento AMQP (Consumer: consumidor_solicitudes)"| SOL

    %% Persistencia Database-per-Service
    CAT -->|"SQL JDBC (Puerto 5432)"| RDS_CAT
    SOL -->|"SQL JDBC (Puerto 5432)"| RDS_SOL
```

---

## 2. Matriz de Responsabilidad, Publicación y Propiedad de Datos

| Componente | Rol en Mensajería | Rol de Datos / Persistencia | Acceso de Red / Puertos |
|---|---|---|---|
| **ms-catalogo** (EC2 A) | **PUBLICADOR (Publisher)**: Emite el evento `CategoriaCreadaEvent` al exchange `catalogo.exchange` usando el usuario `publicador_catalogo`. | **PROPIETARIO EXCLUSIVO** de `RDS Catálogo` (`mesatech_catalogo`). Guarda y mantiene las categorías maestras y prioridades. | Puerto 8081 accesible solo internamente dentro de la VPC por el BFF. |
| **ms-solicitudes** (EC2 B) | **CONSUMIDOR (Consumer)**: Escucha la cola `solicitudes.catalogo.queue` con el usuario `consumidor_solicitudes` y procesa el evento con ACK. | **PROPIETARIO EXCLUSIVO** de `RDS Solicitudes` (`mesatech_solicitudes`). Guarda las solicitudes de soporte y la copia local de categorías para integridad referencial. | Puerto 8082 accesible solo internamente dentro de la VPC por el BFF. |
| **RabbitMQ** (EC2 C) | **BROKER DE MENSAJERÍA**: Administra el Virtual Host `mesatech`, almacena los mensajes de forma durable en disco (`mesatech_rabbitmq_data`). | **NO GUARDA DATOS DE NEGOCIO**. Solo retiene temporalmente los mensajes hasta su consumo exitoso. | Puerto 5672 interno (AMQP) y puerto 15672 público (Web Management). |
| **BFF** (EC2 A) | **COORDINADOR / GATEWAY**: No participa directamente en la mensajería AMQP. | **SIN BASE DE DATOS**. El BFF no accede a ninguna base de datos; coordina llamadas REST a los microservicios. | Puerto 8080 público hacia API Gateway. |
