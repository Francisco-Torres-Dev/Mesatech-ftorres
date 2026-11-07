# MesaTech Cloud â€” EvaluaciÃ³n Parcial N.Â° 2
**Asignatura:** Desarrollo Cloud Native I (DSY1107)  
**Integrantes:** Francisco Torres â€” Kevin MesÃ­as  
**Repositorio GitHub:** [https://github.com/Francisco-Torres-Dev/Mesatech-ftorres](https://github.com/Francisco-Torres-Dev/Mesatech-ftorres)

---

## 1. Resumen de la SoluciÃ³n y Arquitectura

MesaTech Cloud es una plataforma Cloud Native para la gestiÃ³n de solicitudes de soporte tÃ©cnico empresarial. En esta EvaluaciÃ³n Parcial 2 (EP2), el sistema evolucionÃ³ hacia una **Arquitectura Desacoplada Orientada a Eventos (EDA)** con el patrÃ³n **Database-per-Service**:

1. **Frontend:** React 18 + Vite con `@azure/msal-browser` y `@azure/msal-react`, autenticado vÃ­a Microsoft Entra ID.
2. **PerÃ­metro de Seguridad:** AWS HTTP API Gateway (`13fetz3wok`) con JWT Authorizer conectado a Entra ID y soporte CORS preflight.
3. **BFF (Backend for Frontend):** Spring Boot en EC2 A (puerto 8080) actuando como OAuth2 Resource Server.
4. **Microservicio CatÃ¡logo (Publisher):** Spring Boot en EC2 A (puerto 8081). Persiste en **RDS CatÃ¡logo** y publica el evento `categoria.creada` a RabbitMQ.
5. **Microservicio Solicitudes (Consumer):** Spring Boot en EC2 B (puerto 8082). Escucha la cola en RabbitMQ, persiste su copia en **RDS Solicitudes** y gestiona el ciclo de vida de las atenciones.
6. **Broker de MensajerÃ­a:** RabbitMQ 3.13 con Docker Compose en EC2 C (puertos 5672 y 15672) con virtual host `mesatech` y recursos durables.
7. **Bases de Datos:** Dos instancias Amazon RDS independientes (PostgreSQL 16.9) sin esquemas compartidos ni lecturas cruzadas.

---

## 2. Requisitos y Versiones del Stack TecnolÃ³gico

| Componente | Software / Plataforma | VersiÃ³n MÃ­nima |
|---|---|---|
| **Lenguaje Backend** | Java OpenJDK | 21.0.x LTS |
| **Framework Backend** | Spring Boot | 3.3.4 |
| **Gestor de ConstrucciÃ³n** | Apache Maven | 3.9.x |
| **Frontend Web** | Node.js / npm | Node 20+ / npm 10+ |
| **Framework Frontend** | React / Vite | React 18.2 / Vite 5.x |
| **LibrerÃ­a de Auth** | MSAL Browser | 4.30.x |
| **Motor de MensajerÃ­a** | RabbitMQ Management | 3.13.x (Erlang 26) |
| **Contenedores** | Docker Engine & Docker Compose | Docker 27.x / Compose 2.x |
| **Bases de Datos** | PostgreSQL (Amazon RDS) | 16.9 |
| **Sistema Operativo EC2** | Ubuntu Server | 24.04 LTS (Noble) |

---

## 3. Instrucciones de CompilaciÃ³n y Empaquetado

### 3.1 Compilar los Microservicios Java (Maven)
En cada carpeta de microservicio (`ms-catalogo`, `ms-solicitudes`, `bff`):
```bash
# Limpiar y empaquetar sin ejecutar tests locales que requieran BD conectada
mvn clean package -DskipTests
```
El archivo JAR generado se ubicarÃ¡ en la carpeta `target/`:
- `bff/target/bff-0.0.1-SNAPSHOT.jar`
- `ms-catalogo/target/ms-catalogo-0.0.1-SNAPSHOT.jar`
- `ms-solicitudes/target/ms-solicitudes-0.0.1-SNAPSHOT.jar`

### 3.2 Construir el Frontend React
```bash
cd frontend
npm install
npm run build
```
Para ejecutar el servidor de desarrollo local:
```bash
npm run dev -- --host 0.0.0.0
```

---

## 4. ConfiguraciÃ³n de Variables de Entorno

Copiar el archivo de plantilla `.env.example` y ajustar segÃºn el entorno:
- En **modo local**: utiliza los perfiles Spring `local`.
- En **modo AWS**: utiliza los perfiles Spring `-Dspring.profiles.active=aws` definidos en los servicios de systemd.

> **Importante de Seguridad:**  
> Ninguna contraseÃ±a real, secreto de cliente de Microsoft Entra ID ni clave privada SSH (`.pem`) se encuentra versionada en el repositorio Git.

---

## 5. Orden Obligatorio de Arranque, DetenciÃ³n y RecuperaciÃ³n

Para garantizar la integridad y evitar errores de conexiÃ³n al iniciar el sistema distribuido, se debe seguir estrictamente este orden:

### 5.1 Orden de Arranque (Startup Sequence)
1. **Paso 1: Bases de Datos Amazon RDS:**  
   Verificar que `mesatech-rds-catalogo` y `mesatech-rds-solicitudes` estÃ©n en estado `Available`.
2. **Paso 2: Broker RabbitMQ (EC2 C):**  
   Iniciar el contenedor Docker en EC2 C:
   ```bash
   cd ~/rabbitmq && sudo docker compose up -d
   ```
   *Comprobar que el puerto 5672 y 15672 estÃ©n respondiendo.*
3. **Paso 3: Microservicios de Dominio:**  
   - En **EC2 A**: `sudo systemctl start mesatech-catalogo`
   - En **EC2 B**: `sudo systemctl start mesatech-solicitudes`
4. **Paso 4: BFF (EC2 A):**  
   `sudo systemctl start mesatech-bff`
5. **Paso 5: Frontend React:**  
   Iniciar el servidor Vite (`npm run dev`) en el navegador.

---

### 5.2 Orden de DetenciÃ³n (Shutdown Sequence)
1. Detener Frontend React (cerrar proceso Vite).
2. Detener BFF en EC2 A (`sudo systemctl stop mesatech-bff`).
3. Detener Microservicios (`sudo systemctl stop mesatech-catalogo` y `sudo systemctl stop mesatech-solicitudes`).
4. Detener RabbitMQ en EC2 C (`cd ~/rabbitmq && sudo docker compose down`).
5. Detener instancias EC2 y pausar instancias RDS.

---

### 5.3 RecuperaciÃ³n ante CaÃ­das (Disaster Recovery)
- **Si se cae RabbitMQ (Broker Down):**  
  Al reiniciar RabbitMQ (`sudo docker compose up -d`), el volumen `mesatech_rabbitmq_data` recupera automÃ¡ticamente los mensajes pendientes, los exchanges y las colas sin pÃ©rdida de datos.
- **Si se cae el consumidor (Solicitudes Down):**  
  Los eventos se acumulan de manera segura en `solicitudes.catalogo.queue`. Cuando el servicio de solicitudes vuelve a estar en lÃ­nea, drena y procesa todos los mensajes acumulados en orden (consumo diferido).

---

## 6. Contrato del Evento `categoria.creada`

Cuando se crea una categorÃ­a en `ms-catalogo`, se emite un mensaje JSON al exchange `catalogo.exchange` con la routing key `categoria.creada`.

### 6.1 Esquema del Payload JSON
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "CategoriaCreadaEvent",
  "type": "object",
  "properties": {
    "id": { "type": "integer", "description": "Identificador Ãºnico asignado por CatÃ¡logo" },
    "nombre": { "type": "string", "description": "Nombre de la categorÃ­a" },
    "descripcion": { "type": "string", "description": "DescripciÃ³n funcional" },
    "activo": { "type": "boolean", "description": "Estado de vigencia" },
    "timestamp": { "type": "string", "format": "date-time", "description": "Fecha y hora ISO del evento" }
  },
  "required": ["id", "nombre", "activo"]
}
```

### 6.2 Ejemplo Real de Mensaje Emitido
```json
{
  "id": 4,
  "nombre": "Ciberseguridad",
  "descripcion": "Gestion de incidentes y firewall",
  "activo": true,
  "timestamp": "2026-10-30T10:30:15.120Z"
}
```

---

## 7. CatÃ¡logo de Endpoints de la API

Todas las rutas son consumidas a travÃ©s del AWS HTTP API Gateway (`https://13fetz3wok.execute-api.us-east-1.amazonaws.com`):

| MÃ©todo | Endpoint | DescripciÃ³n | Versionamiento |
|---|---|---|---|
| `GET` | `/v1/catalogo` | Retorna categorÃ­as y prioridades activas | V1 Base |
| `POST` | `/v1/catalogo/categorias` | Crea una nueva categorÃ­a y emite el evento AMQP | V1 Base |
| `GET` | `/v1/solicitudes` | Lista todas las solicitudes de soporte | V1 Base |
| `GET` | `/v1/solicitudes/mias` | Lista las solicitudes del usuario autenticado | V1 Base |
| `POST` | `/v1/solicitudes` | Crea una nueva solicitud asociada a una categorÃ­a | V1 Base |
| `PUT` | `/v1/solicitudes/{id}/estado` | TransiciÃ³n de estado (`ASIGNADA`, `EN_PROCESO`, `RESUELTA`) | V1 Base |
| `GET` | `/v2/solicitudes/{id}` | Consulta enriquecida con trazabilidad SLA y contrato extendido | **V2 Coexistente** |

---

## 8. Scripts SQL de InicializaciÃ³n

Los scripts completos de DDL y datos de prueba se encuentran en la carpeta `database/`:
- `database/init-catalogo.sql`: Tablas `categorias` y `prioridades` para `mesatech-rds-catalogo`.
- `database/init-solicitudes.sql`: Tablas `usuarios`, `categorias` (copia local sin autoincremento), `prioridades` y `solicitudes` para `mesatech-rds-solicitudes`.

