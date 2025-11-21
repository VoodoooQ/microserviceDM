-- =====================================================
-- SCRIPT SQL PARA CREAR LA TABLA PETS EN SUPABASE
-- =====================================================
-- Compatible con PostgreSQL 13+
-- Ejecuta este script en: Supabase > SQL Editor > New query

-- Crear tabla pets
CREATE TABLE IF NOT EXISTS pets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice para mejorar búsquedas por user_email
CREATE INDEX IF NOT EXISTS idx_pets_user_email ON pets(user_email);

-- Crear función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Crear trigger para updated_at
DROP TRIGGER IF EXISTS update_pets_updated_at ON pets;
CREATE TRIGGER update_pets_updated_at
    BEFORE UPDATE ON pets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- DATOS DE PRUEBA (OPCIONAL)
-- =====================================================
-- Descomentar las siguientes líneas para insertar datos de prueba

-- INSERT INTO pets (name, type, user_email) VALUES
-- ('Rex', 'dog', 'user@example.com'),
-- ('Michi', 'cat', 'user@example.com'),
-- ('Bobby', 'dog', 'otro@example.com'),
-- ('Luna', 'cat', 'user@example.com');

-- =====================================================
-- CONSULTAS ÚTILES
-- =====================================================

-- Ver todas las mascotas
-- SELECT * FROM pets;

-- Ver mascotas por usuario
-- SELECT * FROM pets WHERE user_email = 'user@example.com';

-- Contar mascotas por tipo
-- SELECT type, COUNT(*) as total FROM pets GROUP BY type;

-- Eliminar todos los datos (CUIDADO)
-- TRUNCATE TABLE pets RESTART IDENTITY CASCADE;

-- Eliminar la tabla (CUIDADO)
-- DROP TABLE IF EXISTS pets CASCADE;
