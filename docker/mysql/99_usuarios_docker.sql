-- =============================================================================
-- Usuarios para conexiones desde otros contenedores.
-- Los scripts originales crean los usuarios con host 'localhost', que solo
-- permite conexiones desde el mismo contenedor de MySQL. En Docker cada API
-- se conecta desde otro contenedor, por eso se crean también con host '%'.
-- =============================================================================
CREATE USER IF NOT EXISTS 'usuario_citas'@'%' IDENTIFIED BY 'ISO/IEC27001';
GRANT ALL PRIVILEGES ON bd_citasmedicas_v321.* TO 'usuario_citas'@'%';

CREATE USER IF NOT EXISTS 'usuario_farmacia'@'%' IDENTIFIED BY 'Farmacia2026*';
GRANT ALL PRIVILEGES ON bd_famacia.* TO 'usuario_farmacia'@'%';

FLUSH PRIVILEGES;
