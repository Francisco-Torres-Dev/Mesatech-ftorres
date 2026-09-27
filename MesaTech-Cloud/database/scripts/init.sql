CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    rol VARCHAR(40) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prioridades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    nivel INTEGER NOT NULL CHECK (nivel > 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS solicitudes (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria_id BIGINT NOT NULL,
    prioridad_id BIGINT NOT NULL,
    usuario_solicitante_id UUID NOT NULL,
    estado VARCHAR(30) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    usuario_asignado_id UUID,
    detalle_atencion TEXT,
    asignado_a VARCHAR(150),
    CONSTRAINT fk_solicitudes_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    CONSTRAINT fk_solicitudes_prioridad FOREIGN KEY (prioridad_id) REFERENCES prioridades(id),
    CONSTRAINT fk_solicitudes_usuario FOREIGN KEY (usuario_solicitante_id) REFERENCES usuarios(id),
    CONSTRAINT fk_solicitudes_usuario_asignado FOREIGN KEY (usuario_asignado_id) REFERENCES usuarios(id)
);

ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS observacion TEXT;

ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS usuario_asignado_id UUID;

ALTER TABLE usuarios OWNER TO mesatech;
ALTER TABLE categorias OWNER TO mesatech;
ALTER TABLE prioridades OWNER TO mesatech;
ALTER TABLE solicitudes OWNER TO mesatech;
ALTER TABLE usuarios ALTER COLUMN id SET DEFAULT gen_random_uuid();

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_solicitudes_usuario_asignado'
    ) THEN
        ALTER TABLE solicitudes
            ADD CONSTRAINT fk_solicitudes_usuario_asignado
            FOREIGN KEY (usuario_asignado_id) REFERENCES usuarios(id);
    END IF;
END $$;

INSERT INTO usuarios (id, nombre, email, rol, activo)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Cliente Demo', 'cliente@mesatech.local', 'CLIENTE', TRUE),
    ('22222222-2222-2222-2222-222222222222', 'Operador Demo', 'operador@mesatech.local', 'OPERADOR', TRUE),
    ('33333333-3333-3333-3333-333333333333', 'Admin Demo', 'admin@mesatech.local', 'ADMINISTRADOR', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, activo)
VALUES
    ('Hardware', 'Fallas de equipos y periféricos', TRUE),
    ('Software', 'Errores de aplicaciones y sistemas', TRUE),
    ('Redes', 'Problemas de conectividad y enrutamiento', TRUE),
    ('Correo', 'Servicios de correo y autenticación', TRUE),
    ('Seguridad', 'Incidentes de seguridad o accesos', TRUE)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO prioridades (nombre, nivel, activo)
VALUES
    ('BAJA', 1, TRUE),
    ('MEDIA', 2, TRUE),
    ('ALTA', 3, TRUE),
    ('CRITICA', 4, TRUE)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO solicitudes (titulo, descripcion, categoria_id, prioridad_id, usuario_solicitante_id, estado, fecha_creacion, fecha_actualizacion, detalle_atencion)
SELECT 'No enciende equipo', 'El equipo de la oficina no prende luego de un corte eléctrico.', c.id, p.id, u.id, 'CREADA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL
FROM categorias c, prioridades p, usuarios u
WHERE c.nombre = 'Hardware' AND p.nombre = 'MEDIA' AND u.email = 'cliente@mesatech.local'
ON CONFLICT DO NOTHING;

INSERT INTO solicitudes (titulo, descripcion, categoria_id, prioridad_id, usuario_solicitante_id, estado, fecha_creacion, fecha_actualizacion, detalle_atencion)
SELECT 'Correo caído', 'No puedo enviar correos desde la cuenta corporativa.', c.id, p.id, u.id, 'EN_PROCESO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Se revisa el servicio de correo y la configuración del cliente.'
FROM categorias c, prioridades p, usuarios u
WHERE c.nombre = 'Correo' AND p.nombre = 'ALTA' AND u.email = 'cliente@mesatech.local'
ON CONFLICT DO NOTHING;
