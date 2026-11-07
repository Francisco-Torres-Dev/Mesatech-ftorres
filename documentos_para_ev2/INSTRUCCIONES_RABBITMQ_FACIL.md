# GuÃ­a FÃ¡cil de RabbitMQ para MesaTech Cloud (EP2)
**Integrantes:** Francisco Torres â€” Kevin MesÃ­as  
**Asignatura:** Desarrollo Cloud Native I (DSY1107)

---

## 1. Â¿QuÃ© es RabbitMQ y por quÃ© lo usamos?

En la primera evaluaciÃ³n (EP1), cuando un usuario creaba una categorÃ­a, un servicio llamaba directamente al otro por internet de forma sÃ­ncrona (como una llamada telefÃ³nica). Si el segundo servicio estaba caÃ­do o lento, toda la operaciÃ³n fallaba o se quedaba esperando.

En la EvaluaciÃ³n 2 (EP2), implementamos **mensajerÃ­a asÃ­ncrona con RabbitMQ**. Ahora los microservicios no se llaman directamente entre sÃ­: se comunican mediante mensajes (como enviar cartas). 
- **CatÃ¡logo (Publisher)** crea la categorÃ­a en su base de datos y deja una carta en la oficina postal (RabbitMQ).
- **Solicitudes (Consumer)** recoge la carta cuando estÃ¡ disponible, lee los datos y guarda su propia copia local.
- **Si Solicitudes estÃ¡ apagado:** Las cartas no se pierden; quedan esperando pacientemente en la cola de RabbitMQ hasta que el servicio vuelva a encenderse.

---

## 2. Conceptos Clave Explicados con la AnalogÃ­a de la Oficina Postal

Para defender este proyecto ante el profesor sin complicarte con tÃ©rminos raros, aprÃ©ndete esta analogÃ­a:

| TÃ©rmino TÃ©cnico | En la Vida Real (Oficina Postal) | Â¿QuÃ© hace en MesaTech Cloud? |
|---|---|---|
| **Broker** | El edificio de Correos de Chile | El servidor RabbitMQ instalado en la mÃ¡quina **EC2 C** (`34.236.12.11`). |
| **Virtual Host (`mesatech`)** | Una sucursal privada exclusiva | Un casillero cerrado dentro de RabbitMQ para que nadie mÃ¡s toque los mensajes de nuestra empresa. |
| **Exchange (`catalogo.exchange`)** | La mesa de clasificaciÃ³n de cartas | Recibe el mensaje que envÃ­a CatÃ¡logo y revisa su etiqueta (`categoria.creada`) para decidir a quÃ© buzÃ³n tirarlo. |
| **Queue (`solicitudes.catalogo.queue`)** | El buzÃ³n del destinatario | Es donde se apilan las cartas hasta que el servicio de Solicitudes viene a leerlas. |
| **Binding (Enlace)** | La regla del clasificador | La orden escrita: *"Toda carta con la etiqueta `categoria.creada` debe ir al buzÃ³n `solicitudes.catalogo.queue`"*. |
| **Routing Key (`categoria.creada`)** | La etiqueta o cÃ³digo postal de la carta | El asunto del mensaje que identifica quÃ© tipo de evento acaba de ocurrir. |
| **ACK (Acknowledgment)** | La firma de entrega ("Recibido Conforme") | Solicitudes le dice a RabbitMQ: *"Ya leÃ­ la carta y la guardÃ© en mi base de datos RDS, ahora sÃ­ puedes borrarla del buzÃ³n"*. |
| **Durable (Persistencia)** | Guardar los buzones bajo llave en caja fuerte | Si se corta la luz o reiniciamos el servidor de RabbitMQ, la cola y las cartas no se borran porque estÃ¡n escritas en el disco duro (volumen de Docker). |

---

## 3. Â¿Por quÃ© usamos 2 usuarios diferentes en RabbitMQ? (Principio de MÃ­nimo Privilegio)

El profesor siempre pregunta esto en la defensa:  
**Â¿Por quÃ© no usar el mismo usuario 'admin' para todo?**

**Respuesta:**  
Por seguridad (Principio de Menor Privilegio). Cada aplicaciÃ³n solo debe tener los permisos estrictamente necesarios para hacer su trabajo:
1. **`publicador_catalogo` (CatÃ¡logo):** Solo tiene permiso de **ESCRITURA** en el Exchange. Solo puede despachar cartas; no tiene permiso para leerlas ni para borrarlas.
2. **`consumidor_solicitudes` (Solicitudes):** Solo tiene permiso de **LECTURA** en la Cola. Solo puede retirar cartas de su buzÃ³n; no puede publicar eventos ni modificar los exchanges.
3. **`admin_mesatech` (Administrador):** Es el jefe de la oficina postal con acceso al panel web para monitorear, crear colas y resolver incidentes.

---

## 4. Â¿CÃ³mo se InstalÃ³ RabbitMQ desde Cero en AWS?

En la mÃ¡quina virtual **EC2 C (`34.236.12.11`)** se realizaron estos pasos con Docker Compose:

1. **Se creÃ³ el archivo `compose.yaml`:**
   Usa la imagen oficial `rabbitmq:3.13-management` que incluye el motor de mensajerÃ­a (puerto `5672`) y el panel web grÃ¡fico (puerto `15672`).
2. **Se configurÃ³ un volumen persistente:**  
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

## 5. Datos para Abrir y Mostrar RabbitMQ en la PresentaciÃ³n

Puedes abrir el panel desde cualquier computador con internet:

- **Enlace Web:** [http://34.236.12.11:15672](http://34.236.12.11:15672)
- **Usuario:** `admin_mesatech`
- **ContraseÃ±a:** `admin_password_123`

### Â¿QuÃ© mostrarle al profesor en vivo o en las capturas?
1. **PestaÃ±a Overview:** Muestra el grÃ¡fico de mensajes y las conexiones activas.
2. **PestaÃ±a Exchanges:** Muestra `catalogo.exchange` de tipo `direct` y marcado como `durable: true`.
3. **PestaÃ±a Queues:** Muestra `solicitudes.catalogo.queue`. Si haces clic en ella, verÃ¡s que tiene 1 consumidor conectado (`mesatech-solicitudes` en EC2 B) y `0 messages ready` en reposo.
4. **Virtual Host:** En la esquina superior derecha selecciona siempre el virtual host `mesatech`.

