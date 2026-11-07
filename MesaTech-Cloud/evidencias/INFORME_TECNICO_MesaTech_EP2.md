# INFORME TÉCNICO Y DE EVIDENCIAS
## Evaluación Parcial N.° 2: MesaTech Cloud con Mensajería y Persistencia Independiente
**Asignatura:** Desarrollo Cloud Native I (DSY1107)  
**Fecha:** 30 de Octubre de 2026  
**Proyecto:** MesaTech Cloud  

---

## 1. Resumen Ejecutivo y Cambios Respecto a la EP1

En la Evaluación Parcial 1 (EP1), la arquitectura concentraba los componentes de negocio en una topología compartida, permitiendo una base de datos única o compartida entre microservicios.

Para esta **Evaluación Parcial 2 (EP2)**, se implementó una evolución hacia una arquitectura distribuida basada en eventos asíncronos y persistencia completamente desacoplada ("Database-per-Service"):
1. **Separación Estricta de Cómputo (EC2):** Los microservicios de Catálogo y Solicitudes se ejecutan en instancias EC2 totalmente independientes. El broker RabbitMQ se aloja en una tercera instancia EC2 exclusiva con Ubuntu 24.04 LTS y Docker Compose.
2. **Persistencia Independiente (RDS):** Se sustituyó la base de datos compartida por **dos instancias exclusivas de Amazon RDS** (PostgreSQL 16), cada una con su propio endpoint, credenciales y accesible únicamente por su microservicio propietario.
3. **Integración Asíncrona por Eventos (RabbitMQ):** La propagación de nuevas categorías se realiza mediante eventos persistentes en formato JSON a través de un exchange direct durable y una cola durable en RabbitMQ, eliminando llamadas HTTP síncronas o consultas SQL cruzadas entre servicios.
4. **Continuidad de Seguridad:** Se mantuvo el frontend en React con MSAL, la autenticación contra Microsoft Entra ID y el borde protegido con **AWS API Gateway (HTTP API)** con JWT Authorizer y CORS.

---

## 2. Diagrama de Arquitectura de Despliegue Real

```mermaid
flowchart TD
    subgraph Client_Side ["Cliente / Navegador"]
        ReactApp["Frontend React + MSAL<br/>(localhost:5173 / 3000)"]
    end

    subgraph Identity_Provider ["Seguridad e Identidad"]
        EntraID["Microsoft Entra ID (Azure)<br/>Tenant: 2b28c657...<br/>Client: 964cf08d..."]
    end

    subgraph AWS_Cloud ["AWS Cloud (us-east-1)"]
        subgraph Edge_Layer ["Capa de Borde"]
            APIGW["AWS API Gateway (HTTP API)<br/>Endpoint: 13fetz3wok...<br/>• JWT Authorizer (Entra ID)<br/>• CORS (*)<br/>• Rutas /v1/* y /v2/*"]
        end

        subgraph EC2_A ["EC2 A: MesaTech-EC2-A-Catalogo-BFF (i-05a7867f9d3c7ebaf)"]
            BFF["BFF Spring Boot<br/>(Puerto 8080)<br/>Resource Server JWT"]
            Catalogo["ms-catalogo Spring Boot<br/>(Puerto 8081)<br/>Publicador AMQP"]
        end

        subgraph EC2_B ["EC2 B: MesaTech-EC2-B-Solicitudes (i-0264b18bc3f0955a3)"]
            Solicitudes["ms-solicitudes Spring Boot<br/>(Puerto 8082)<br/>Consumidor AMQP Listener"]
        end

        subgraph EC2_C ["EC2 C: MesaTech-EC2-C-RabbitMQ (i-0073a1baff203a9b4)"]
            Rabbit["RabbitMQ 3.13-management<br/>(Docker Compose)<br/>• VHost: mesatech<br/>• Exchange: catalogo.exchange<br/>• Cola: solicitudes.catalogo.queue<br/>• Puerto AMQP: 5672<br/>• Puerto UI: 15672"]
        end

        subgraph RDS_A ["Amazon RDS Catálogo (mesatech-rds-catalogo)"]
            RDSCat["PostgreSQL 16.9 (5432)<br/>BD: mesatech_catalogo<br/>Tablas: categorias, prioridades"]
        end

        subgraph RDS_B ["Amazon RDS Solicitudes (mesatech-rds-solicitudes)"]
            RDSSol["PostgreSQL 16.9 (5432)<br/>BD: mesatech_solicitudes<br/>Tablas: solicitudes, categorias (copia),<br/>prioridades, usuarios"]
        end
    end

    %% Flujos de Comunicación
    ReactApp -->|1. Login OAuth2 / OIDC| EntraID
    EntraID -->|2. Emite Access Token JWT| ReactApp
    ReactApp -->|3. HTTPS Bearer JWT| APIGW
    APIGW -->|4. HTTP Proxy autorizada| BFF
    BFF -->|5. HTTP localhost:8081| Catalogo
    BFF -->|6. HTTP Privado 172.31.4.52:8082| Solicitudes

    Catalogo -->|7. SQL (HikariCP / JPA)| RDSCat
    Catalogo -->|8. Publicación AMQP persistente con Confirm| Rabbit
    Rabbit -->|9. Consumo asíncrono @RabbitListener| Solicitudes
    Solicitudes -->|10. SQL (HikariCP / JPA - copia local)| RDSSol
```

---

## 3. Inventario de Infraestructura AWS y Matriz de Red

### 3.1 Instancias EC2 Desplegadas
| Componente | Nombre de Instancia | Instance ID | IP Privada | IP Pública | Tipo | Sistema Operativo | Security Group |
|---|---|---|---|---|---|---|---|
| **Catálogo y BFF** | `MesaTech-EC2-A-Catalogo-BFF` | `i-05a7867f9d3c7ebaf` | `172.31.5.17` | `3.239.211.61` | `t3.small` | Ubuntu 24.04 LTS | `sg-03d8ffcf9973089f0` |
| **Solicitudes** | `MesaTech-EC2-B-Solicitudes` | `i-0264b18bc3f0955a3` | `172.31.4.52` | `52.3.230.166` | `t3.micro` | Ubuntu 24.04 LTS | `sg-07fbba9ca5edd0afb` |
| **RabbitMQ Broker** | `MesaTech-EC2-C-RabbitMQ` | `i-0073a1baff203a9b4` | `172.31.13.15` | `100.53.54.131` | `t3.micro` | Ubuntu 24.04 LTS | `sg-0b13179b859054389` |

### 3.2 Instancias Amazon RDS Exclusivas
| Instancia RDS | Endpoint | Puerto | Motor | Base de Datos | Usuario | Acceso Público | Security Group |
|---|---|---|---|---|---|---|---|
| **RDS Catálogo** | `mesatech-rds-catalogo.cqshchood3yx.us-east-1.rds.amazonaws.com` | `5432` | PostgreSQL 16.9 | `mesatech_catalogo` | `mesatech` | **NO** | `sg-05c509b1d00eaf639` |
| **RDS Solicitudes** | `mesatech-rds-solicitudes.cqshchood3yx.us-east-1.rds.amazonaws.com` | `5432` | PostgreSQL 16.9 | `mesatech_solicitudes` | `mesatech` | **NO** | `sg-00f4ba0465ecaf653` |

### 3.3 Matriz de Comunicaciones y Reglas de Seguridad (Security Groups)
| Destino (Security Group) | Puerto | Protocolo | Origen Autorizado | Justificación Técnica |
|---|---|---|---|---|
| `mesatech-sg-rabbitmq` | `22` | TCP | `0.0.0.0/0` (o IP admin) | Administración segura vía SSH |
| `mesatech-sg-rabbitmq` | `5672` | TCP | `sg-03d8ffcf9973089f0`, `sg-07fbba9ca5edd0afb` | Conexión AMQP privada exclusiva para Catálogo y Solicitudes |
| `mesatech-sg-rabbitmq` | `15672` | TCP | `0.0.0.0/0` (o IP admin) | Interfaz web de administración de RabbitMQ |
| `mesatech-sg-catalogo` | `22` | TCP | `0.0.0.0/0` | SSH administrativo |
| `mesatech-sg-catalogo` | `8080` | TCP | `0.0.0.0/0` | Punto de entrada HTTP proxy desde AWS API Gateway hacia el BFF |
| `mesatech-sg-catalogo` | `8081` | TCP | `sg-03d8ffcf9973089f0` (localhost/VPC) | Endpoint interno de Catálogo invocado por el BFF |
| `mesatech-sg-solicitudes` | `22` | TCP | `0.0.0.0/0` | SSH administrativo |
| `mesatech-sg-solicitudes` | `8082` | TCP | `sg-03d8ffcf9973089f0` (EC2 Catálogo/BFF) | Endpoint interno de Solicitudes invocado por el BFF |
| `mesatech-sg-rds-catalogo` | `5432` | TCP | `sg-03d8ffcf9973089f0` (EC2 Catálogo) | Acceso SQL exclusivo al microservicio Catálogo |
| `mesatech-sg-rds-solicitudes` | `5432` | TCP | `sg-07fbba9ca5edd0afb` (EC2 Solicitudes) | Acceso SQL exclusivo al microservicio Solicitudes |

---

## 4. Instalación de RabbitMQ desde Cero y Recursos de Mensajería

### 4.1 Archivo compose.yaml
```yaml
services:
  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: mesatech-rabbitmq
    hostname: rabbitmq.mesatech.internal
    restart: unless-stopped
    env_file:
      - .env
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  rabbitmq_data:
    name: mesatech_rabbitmq_data
```

### 4.2 Recursos de Mensajería Declarados
* **Virtual Host:** `mesatech`
* **Usuarios:**
  * `publicador_catalogo`: Permisos `.* .* .*` sobre `mesatech`.
  * `consumidor_solicitudes`: Permisos `.* .* .*` sobre `mesatech`.
  * `admin_mesatech`: Rol de administrador.
* **Exchange:** `catalogo.exchange` (tipo: `direct`, `durable: true`)
* **Cola:** `solicitudes.catalogo.queue` (`durable: true`, `auto_delete: false`)
* **Binding:** `catalogo.exchange` -> `solicitudes.catalogo.queue` con routing key `categoria.creada`.

---

## 5. Contrato del Evento Asíncrono

### 5.1 Especificación JSON
Al crearse una categoría en Catálogo, se emite el siguiente mensaje persistente:
```json
{
  "eventId": "2e29c2b0-e117-42a0-9157-cf052c29bd12",
  "tipoEvento": "CATEGORIA_CREADA",
  "fechaEvento": "2026-10-30T10:29:48.347Z",
  "categoriaId": 4,
  "nombre": "Ciberseguridad"
}
```

### 5.2 Tipos y Descripción de Campos
| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `eventId` | String (UUID v4) | Sí | Identificador único del evento para trazabilidad e idempotencia |
| `tipoEvento` | String | Sí | Tipo de acción (`CATEGORIA_CREADA`) |
| `fechaEvento` | String (ISO-8601 UTC) | Sí | Timestamp de emisión del evento en el publicador |
| `categoriaId` | Long / BigInt | Sí | Clave primaria generada por RDS Catálogo. Se preserva exactamente en RDS Solicitudes |
| `nombre` | String | Sí | Nombre de la categoría (ej. "Hardware", "Ciberseguridad") |

---

## 6. Resultados de las Pruebas Obligatorias (P1 a P8)

### P1: Separación Real
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P1_separacion_real.txt`.
* **Resultado:** Se demostró la existencia de 3 instancias EC2 (`i-05a7867f9d3c7ebaf`, `i-0264b18bc3f0955a3`, `i-0073a1baff203a9b4`) y 2 endpoints RDS totalmente distintos (`mesatech-rds-catalogo...` y `mesatech-rds-solicitudes...`). No existen bases compartidas ni lecturas SQL cruzadas.

### P2: Flujo Completo
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P2_flujo_completo.txt`.
* **Resultado:** Creación de categoría `Ciberseguridad` (ID 4) en Catálogo -> guardada en RDS Catálogo -> confirmación ACK en RabbitMQ -> Solicitudes recibe el evento -> persiste la copia en RDS Solicitudes con ID 4 -> se crea solicitud de soporte con categoría 4 exitosamente.

### P3: Consumidor Detenido
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P3_consumidor_detenido.txt`.
* **Resultado:** Servicio consumidor detenido (`sudo systemctl stop mesatech-solicitudes`). Se crea categoría `Cloud AWS` (ID 5) en Catálogo. Queda guardada en RDS Catálogo. En RabbitMQ la cola pasa a `messages_ready: 1`. En RDS Solicitudes la consulta retorna `0 rows`.

### P4: Consumo Diferido
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P4_consumo_diferido.txt`.
* **Resultado:** Se levanta el consumidor (`sudo systemctl start mesatech-solicitudes`). Procesa el mensaje retenido, persiste `Cloud AWS` en RDS Solicitudes con ID 5. La cola de RabbitMQ pasa a `messages: 0, messages_ready: 0, messages_unacknowledged: 0`.

### P5: Reinicio del Broker con Volumen Persistente
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P5_reinicio_broker.txt`.
* **Resultado:** Con consumidor detenido, se publica categoría `DevOps y CI/CD` (ID 6). Queda 1 mensaje en cola. Se destruye y recrea el contenedor con `docker compose down` y `docker compose up -d`. Al levantar, el vhost `mesatech`, el exchange, la cola y el mensaje pendiente continúan intactos gracias al volumen `mesatech_rabbitmq_data`. Se inicia el consumidor y el mensaje es procesado y guardado en RDS Solicitudes.

### P6: Reinicio de Aplicaciones
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P6_reinicio_aplicaciones.txt`.
* **Resultado:** Se reinician los servicios (`sudo systemctl restart mesatech-catalogo` y `mesatech-solicitudes`). Las conexiones a RDS se reabren y todas las categorías (1 a 6) y solicitudes registradas se mantienen intactas.

### P7: Fallo de Publicación
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P7_fallo_publicacion.txt`.
* **Resultado:** Se detiene RabbitMQ. Al intentar crear categoría `Monitoreo CloudWatch`, el microservicio responde con error controlado `HTTP 503 Service Unavailable`, informando que la categoría fue guardada en RDS Catálogo (ID 7) pero el broker no estuvo disponible. Se restablece el broker y se explica la inconsistencia eventual.

### P8: Continuidad de EP1
* **Evidencia:** Archivo `evidencias/03-pruebas-p1-a-p8/P8_continuidad_ep1.txt`.
* **Resultado:**
  * Petición sin token en AWS API Gateway es rechazada con `HTTP 401 Unauthorized` por el JWT Authorizer.
  * Cabeceras CORS preflight (`OPTIONS`) operativas.
  * Regla de negocio de estados: transición directa `CREADA -> RESUELTA` es rechazada con `HTTP 409 Conflict` ("solo puede resolverse desde EN_PROCESO"). El flujo `CREADA -> ASIGNADA -> EN_PROCESO -> RESUELTA` se completa correctamente.
  * Coexistencia de versiones: `GET /v1/solicitudes/1` y `GET /v2/solicitudes/1` operativas simultáneamente.

---

## 7. Preguntas Clave para la Defensa Individual (Criterio C9)

1. **¿Qué diferencia existe entre el Publisher Confirm de RabbitMQ y la persistencia del Consumidor en RDS?**
   * *Respuesta:* El Publisher Confirm (`ack` del broker) solo acredita que el broker RabbitMQ recibió el mensaje en su memoria/disco y lo enrutó a la cola correspondiente. **No garantiza** que el consumidor lo haya leído, ni mucho menos que haya completado la transacción SQL en su base de datos RDS. Son dos fronteras de persistencia totalmente independientes.

2. **¿Por qué ocurre el fallo documentado en la prueba P7 (Dual-Write Problem) y cómo se resolvería en un entorno de producción avanzado?**
   * *Respuesta:* Guardar en RDS y publicar en RabbitMQ son dos operaciones transaccionales aisladas sin soporte nativo de commit conjunto. Si la BD confirma y RabbitMQ falla, el dato queda guardado en origen pero desincronizado en destino. En producción avanzada esto se resuelve con el patrón **Transactional Outbox**, persistiendo el evento dentro de la misma transacción de la entidad en una tabla SQL `outbox` y leyéndola con un proceso en segundo plano (o CDC como Debezium) hacia RabbitMQ.

3. **¿Por qué los puertos 5672 y 15672 de RabbitMQ tienen orígenes distintos en la matriz de red?**
   * *Respuesta:* El puerto 5672 utiliza el protocolo AMQP binario y solo debe ser accesible desde la red privada por las instancias EC2 que ejecutan los microservicios autorizados (Catálogo y Solicitudes) mediante sus Security Groups. El puerto 15672 es la interfaz web de gestión HTTP (RabbitMQ Management) y está restringida únicamente a la IP del administrador para monitoreo.
