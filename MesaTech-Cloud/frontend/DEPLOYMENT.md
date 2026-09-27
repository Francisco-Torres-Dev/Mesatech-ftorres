# Frontend en produccion

El build de Vite toma la URL de API Gateway desde `.env.production` mediante `VITE_API_URL`. Las llamadas Axios ya incluyen sus rutas, por ejemplo `/v1/catalogo` y `/v1/solicitudes`; no agregues `/v1` a la URL base.

## Configuracion pendiente en AWS y Microsoft Entra ID

- En la configuracion CORS de API Gateway HTTP API, permite como origen el origen exacto del sitio publicado en EC2/Nginx, incluyendo esquema y puerto si aplica. Conserva `http://localhost:5173` solo para desarrollo si sigue siendo necesario y evita `*` en produccion.
- Permite los metodos usados por el Frontend (`GET`, `POST`, `PUT`) y las cabeceras `Authorization` y `Content-Type`. Las solicitudes con token Bearer y JSON pueden generar una preflight `OPTIONS`; API Gateway debe responderla con los encabezados CORS apropiados. No se usan cookies cross-site, por lo que no se requiere `Access-Control-Allow-Credentials` para este cliente.
- Registra el origen de produccion como Redirect URI de tipo SPA en la aplicacion de Microsoft Entra ID. El build de produccion usa automaticamente el origen actual del sitio; `VITE_ENTRA_REDIRECT_URI` se conserva para desarrollo local, que sigue usando `http://localhost:5173`.
- Si el BFF o API Gateway devuelve encabezados CORS propios, verifica que no contradigan la configuracion de CORS administrada por API Gateway.

## Nginx

Sirve el contenido generado en `dist/` y configura el fallback SPA para que las rutas de React Router se resuelvan a `index.html` (por ejemplo, `try_files $uri $uri/ /index.html;`).