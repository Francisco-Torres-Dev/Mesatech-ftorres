-- Esquema exclusivo para RDS Solicitudes (MesaTech Cloud EP2)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    rol VARCHAR(40) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de copia local de categorías (el id proviene del evento de Catálogo, NO autoincrementado)
CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT PRIMARY KEY,
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
    CONSTRAINT fk_solicitudes_usuario FOREIGN KEY (usuario_solicitante_id) REFERENCES usuarios(id)
);

INSERT INTO usuarios (id, nombre, email, rol, activo)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Cliente Demo', 'cliente@mesatech.local', 'CLIENTE', TRUE),
    ('22222222-2222-2222-2222-222222222222', 'Operador Demo', 'operador@mesatech.local', 'OPERADOR', TRUE),
    ('33333333-3333-3333-3333-333333333333', 'Admin Demo', 'admin@mesatech.local', 'ADMINISTRADOR', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO prioridades (nombre, nivel, activo)
VALUES
    ('BAJA', 1, TRUE),
    ('MEDIA', 2, TRUE),
    ('ALTA', 3, TRUE),
    ('CRITICA', 4, TRUE)
ON CONFLICT (nombre) DO NOTHING;

-- Sincronizamos inicialmente las categorías base con sus mismos IDs
INSERT INTO categorias (id, nombre, descripcion, activo)
VALUES
    (1, 'Hardware', 'Fallas de equipos y periféricos', TRUE),
    (2, 'Software', 'Errores de aplicaciones y sistemas', TRUE),
    (3, 'Redes', 'Problemas de conectividad y enrutamiento', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO solicitudes (titulo, descripcion, categoria_id, prioridad_id, usuario_solicitante_id, estado, fecha_creacion, fecha_actualizacion, detalle_atencion)
SELECT 'No enciende equipo', 'El equipo de la oficina no prende luego de un corte eléctrico.', c.id, p.id, u.id, 'CREADA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL
FROM categorias c, prioridades p, usuarios u
WHERE c.nombre = 'Hardware' AND p.nombre = 'MEDIA' AND u.email = 'cliente@mesatech.local'
ON CONFLICT DO NOTHING;
