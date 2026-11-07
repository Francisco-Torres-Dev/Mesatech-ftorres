# Guía Fácil de RabbitMQ para MesaTech Cloud (EP2)
**Integrantes:** Francisco Torres — Kevin Mesías  
**Asignatura:** Desarrollo Cloud Native I (DSY1107)

---

## 1. ¿Qué es RabbitMQ y por qué lo usamos?

En la primera evaluación (EP1), cuando un usuario creaba una categoría, un servicio llamaba directamente al otro por internet de forma síncrona (como una llamada telefónica). Si el segundo servicio estaba caído o lento, toda la operación fallaba o se quedaba esperando.

En la Evaluación 2 (EP2), implementamos **mensajería asíncrona con RabbitMQ**. Ahora los microservicios no se llaman directamente entre sí: se comunican mediante mensajes (como enviar cartas). 
- **Catálogo (Publisher)** crea la categoría en su base de datos y deja una carta en la oficina postal (RabbitMQ).
- **Solicitudes (Consumer)** recoge la carta cuando está disponible, lee los datos y guarda su propia copia local.
- **Si Solicitudes está apagado:** Las cartas no se pierden; quedan esperando pacientemente en la cola de RabbitMQ hasta que el servicio vuelva a encenderse.

---

## 2. Conceptos Clave Explicados con la Analogía de la Oficina Postal

Para defender este proyecto ante el profesor sin complicarte con términos raros, apréndete esta analogía:

| Término Técnico | En la Vida Real (Oficina Postal) | ¿Qué hace en MesaTech Cloud? |
|---|---|---|
| **Broker** | El edificio de Correos de Chile | El servidor RabbitMQ instalado en la máquina **EC2 C** (`3.226.237.1`). |
| **Virtual Host (`mesatech`)** | Una sucursal privada exclusiva | Un casillero cerrado dentro de RabbitMQ para que nadie más toque los mensajes de nuestra empresa. |
| **Exchange (`catalogo.exchange`)** | La mesa de clasificación de cartas | Recibe el mensaje que envía Catálogo y revisa su etiqueta (`categoria.creada`) para decidir a qué buzón tirarlo. |
| **Queue (`solicitudes.catalogo.queue`)** | El buzón del destinatario | Es donde se apilan las cartas hasta que el servicio de Solicitudes viene a leerlas. |
| **Binding (Enlace)** | La regla del clasificador | La orden escrita: *"Toda carta con la etiqueta `categoria.creada` debe ir al buzón `solicitudes.catalogo.queue`"*. |
| **Routing Key (`categoria.creada`)** | La etiqueta o código postal de la carta | El asunto del mensaje que identifica qué tipo de evento acaba de ocurrir. |
| **ACK (Acknowledgment)** | La firma de entrega ("Recibido Conforme") | Solicitudes le dice a RabbitMQ: *"Ya leí la carta y la guardé en mi base de datos RDS, ahora sí puedes borrarla del buzón"*. |
| **Durable (Persistencia)** | Guardar los buzones bajo llave en caja fuerte | Si se corta la luz o reiniciamos el servidor de RabbitMQ, la cola y las cartas no se borran porque están escritas en el disco duro (volumen de Docker). |

---

## 3. ¿Por qué usamos 2 usuarios diferentes en RabbitMQ? (Principio de Mínimo Privilegio)

El profesor siempre pregunta esto en la defensa:  
**¿Por qué no usar el mismo usuario 'admin' para todo?**

**Respuesta:**  
Por seguridad (Principio de Menor Privilegio). Cada aplicación solo debe tener los permisos estrictamente necesarios para hacer su trabajo:
1. **`publicador_catalogo` (Catálogo):** Solo tiene permiso de **ESCRITURA** en el Exchange. Solo puede despachar cartas; no tiene permiso para leerlas ni para borrarlas.
2. **`consumidor_solicitudes` (Solicitudes):** Solo tiene permiso de **LECTURA** en la Cola. Solo puede retirar cartas de su buzón; no puede publicar eventos ni modificar los exchanges.
3. **`admin_mesatech` (Administrador):** Es el jefe de la oficina postal con acceso al panel web para monitorear, crear colas y resolver incidentes.

---

## 4. ¿Cómo se Instaló RabbitMQ desde Cero en AWS?

En la máquina virtual **EC2 C (`3.226.237.1`)** se realizaron estos pasos con Docker Compose:

1. **Se creó el archivo `compose.yaml`:**
   Usa la imagen oficial `rabbitmq:3.13-management` que incluye el motor de mensajería (puerto `5672`) y el panel web gráfico (puerto `15672`).
2. **Se configuró un volumen persistente:**  
   `mesatech_rabbitmq_data:/var/lib/rabbitmq` para que los mensajes sobrevivan a apagones y reinicios.
3. **Se crearon el virtual host, usuarios y permisos:**
   ```bash
   # 1. Crear el virtual host privado
   rabbitmqctl add_vhost mesatech

   # 2. Crear los dos usuarios de las aplicaciones
   rabbitmqctl add_user publicador_catalogo catalogo_password_123
   rabbitmqctl add_user consumidor_solicitudes solicitudes_password_123

   # 3. Dar permisos justos
   rabbitmqctl set_permissions -p mesatech publicador_catalogo ".*" ".*" ""
   rabbitmqctl set_permissions -p mesatech consumidor_solicitudes ".*" "" ".*"
   ```
4. **Se crearon el Exchange, la Cola y el Binding:**
   - Exchange Direct: `catalogo.exchange` (Durable)
   - Queue: `solicitudes.catalogo.queue` (Durable)
   - Binding: une ambos con la clave `categoria.creada`.

---

## 5. Datos para Abrir y Mostrar RabbitMQ en la Presentación

Puedes abrir el panel desde cualquier computador con internet:

- **Enlace Web:** [http://3.226.237.1:15672](http://3.226.237.1:15672)
- **Usuario:** `admin_mesatech`
- **Contraseña:** `admin_password_123`

### ¿Qué mostrarle al profesor en vivo o en las capturas?
1. **Pestaña Overview:** Muestra el gráfico de mensajes y las conexiones activas.
2. **Pestaña Exchanges:** Muestra `catalogo.exchange` de tipo `direct` y marcado como `durable: true`.
3. **Pestaña Queues:** Muestra `solicitudes.catalogo.queue`. Si haces clic en ella, verás que tiene 1 consumidor conectado (`mesatech-solicitudes` en EC2 B) y `0 messages ready` en reposo.
4. **Virtual Host:** En la esquina superior derecha selecciona siempre el virtual host `mesatech`.
