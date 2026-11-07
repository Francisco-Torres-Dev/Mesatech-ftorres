-- Esquema exclusivo para RDS Catálogo (MesaTech Cloud EP2)
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

INSERT INTO prioridades (nombre, nivel, activo)
VALUES
    ('BAJA', 1, TRUE),
    ('MEDIA', 2, TRUE),
    ('ALTA', 3, TRUE),
    ('CRITICA', 4, TRUE)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, activo)
VALUES
    ('Hardware', 'Fallas de equipos y periféricos', TRUE),
    ('Software', 'Errores de aplicaciones y sistemas', TRUE),
    ('Redes', 'Problemas de conectividad y enrutamiento', TRUE)
ON CONFLICT (nombre) DO NOTHING;
