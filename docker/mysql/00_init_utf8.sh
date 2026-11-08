#!/bin/bash
# =============================================================================
# Inicialización de las bases de datos NovaSalud (solo la primera vez).
# Ejecuta los scripts oficiales de databaseMysql/ forzando UTF-8 en el cliente
# mysql; sin esto se ejecutan como latin1 y las tildes quedan mal guardadas
# (por ejemplo "MÃ©dico" en lugar de "Médico").
# =============================================================================
set -e

SCRIPTS=(
  /novasalud-sql/v321/00_instalacion_completa_bd_citasmedicas_v321.sql
  /novasalud-sql/v321/01_stored_procedures_completos_v321.sql
  /novasalud-sql/Farmacia/00_bd_farmacia_v1.sql
  /novasalud-sql/Farmacia/02_insert_data_farmacia_v1.sql
  /novasalud-sql/Farmacia/03_bd_farmacia_sp_v1.sql
  /novasalud-docker-sql/99_usuarios_docker.sql
)

for f in "${SCRIPTS[@]}"; do
  echo "[NovaSalud] Ejecutando $f"
  mysql --protocol=socket --default-character-set=utf8mb4 -uroot -p"${MYSQL_ROOT_PASSWORD}" < "$f"
done
echo "[NovaSalud] Bases de datos inicializadas correctamente"
