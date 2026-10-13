# Guía Unificada de Despliegue de Bases de Datos - NovaSalud V3.2.1

**Proyecto:** Sistema Clínico y Farmacéutico NovaSalud  
**Arquitectura:** SOA Desacoplada (Database-per-Service)  
**Motor de Base de Datos:** MySQL 8.0+  
**Codificación Global:** `utf8mb4` / `utf8mb4_general_ci`  
**Fecha de Actualización:** 13/10/2026  

---

## 1. Visión General de las Bases de Datos

El sistema NovaSalud opera bajo el patrón arquitectónico **Database-per-Service** con dos bases de datos completamente independientes y desacopladas:

```text
 ┌──────────────────────────────────────────────────┐      ┌──────────────────────────────────────────────────┐
 │ 1. DOMINIO CLÍNICO Y CITAS                       │      │ 2. DOMINIO FARMACÉUTICO                          │
 │ Base de Datos: bd_citasmedicas_v321              │      │ Base de Datos: bd_famacia                        │
 │ Consumidores: novasalud-citas-api (:8080)        │      │ Consumidores: novasalud-farmacia-api (:8081)     │
 │               novasalud-atencion-api (:8082)     │      │               novasalud-farmacia-web (:8081)     │
 │ Usuario BD: usuario_citas / ISO/IEC27001         │      │ Usuario BD: usuario_farmacia / Farmacia2026*     │
 └──────────────────────────────────────────────────┘      └──────────────────────────────────────────────────┘
```

---

## 2. Credenciales y Cuentas de Acceso

| Base de Datos | Usuario MySQL | Contraseña | Privilegios Requeridos | Microservicios que se conectan |
| :--- | :--- | :--- | :--- | :--- |
| **Administración** | `root` | *(según entorno / vacía por defecto)* | Global Admin (DBA) | Instalación y despliegue inicial |
| **`bd_citasmedicas_v321`** | `usuario_citas` | `ISO/IEC27001` | `ALL PRIVILEGES` sobre `bd_citasmedicas_v321.*` | `novasalud-citas-api`<br/>`novasalud-atencion-api` |
| **`bd_famacia`** | `usuario_farmacia` | `Farmacia2026*` | `ALL PRIVILEGES` sobre `bd_famacia.*` | `novasalud-farmacia-api`<br/>`novasalud-farmacia-web` |

---

## 3. PASO A PASO: Despliegue de la Base de Datos de Citas (`bd_citasmedicas_v321`)

Carpeta de scripts: [`v321/`](v321/)

### Paso 1.1: Instalación de Esquema, Tablas y Datos Maestros
* **Script:** [`v321/00_instalacion_completa_bd_citasmedicas_v321.sql`](v321/00_instalacion_completa_bd_citasmedicas_v321.sql)
* **Usuario ejecutor:** `root`
* **Acciones que realiza:**
  1. Elimina y crea la base de datos `bd_citasmedicas_v321`.
  2. Crea las tablas de especialidades, pacientes, médicos, horarios, citas, historia clínica, atención médica, pagos, cajas y auditoría.
  3. Inserta catálogos base, médicos demo, tarifas por especialidad y usuarios administrativos con contraseñas BCrypt.
  4. Crea las vistas de apoyo `vw_citas_completas` y `vw_pagos_completos`.
  5. Crea el usuario `'usuario_citas'@'localhost'` con su contraseña oficial.

### Paso 1.2: Despliegue de Procedimientos Almacenados Transaccionales
* **Script:** [`v321/01_stored_procedures_completos_v321.sql`](v321/01_stored_procedures_completos_v321.sql)
* **Usuario ejecutor:** `usuario_citas` o `root`
* **Acciones que realiza:**
  1. Configura `DELIMITER $$`.
  2. Despliega los 12 procedimientos almacenados oficiales (`sp_registrar_cita`, `sp_actualizar_cita`, `sp_eliminar_cita`, `sp_registrar_paciente`, `sp_registrar_medico`, etc.).
  3. Restaura `DELIMITER ;`.

*(Nota: El archivo adicional `v321/bd_usuario.sql` solo se requiere si en un entorno personalizado se desea reconfigurar el usuario de citas de forma aislada).*

---

## 4. PASO A PASO: Despliegue de la Base de Datos de Farmacia (`bd_famacia`)

Carpeta de scripts: [`Farmacia/`](Farmacia/)

### Paso 2.1: Creación de Esquema, Tablas e Índices
* **Script:** [`Farmacia/00_bd_farmacia_v1.sql`](Farmacia/00_bd_farmacia_v1.sql)
* **Usuario ejecutor:** `root`
* **Acciones que realiza:**
  1. Ejecuta `DROP DATABASE IF EXISTS bd_famacia;`.
  2. Crea la base de datos `bd_famacia` con charset `utf8mb4`.
  3. Crea las 10 tablas en orden estricto de llaves foráneas:
     * `categoria_medicamento` $\rightarrow$ `medicamento` $\rightarrow$ `proveedor`
     * `lote_medicamento` $\rightarrow$ `inventario` $\rightarrow$ `usuario_farmacia`
     * `prescripcion` $\rightarrow$ `prescripcion_detalle`
     * `venta` $\rightarrow$ `venta_detalle`
  4. Crea los índices optimizados para búsquedas rápidas.

### Paso 2.2: Creación de Usuario y Concesión de Permisos
* **Script:** [`Farmacia/01_bd_usuario.sql`](Farmacia/01_bd_usuario.sql)
* **Usuario ejecutor:** `root`
* **Acciones que realiza:**
  1. Crea el usuario `'usuario_farmacia'@'localhost'` con contraseña `'Farmacia2026*'`.
  2. Asigna `GRANT ALL PRIVILEGES ON bd_famacia.*`.
  3. Aplica `FLUSH PRIVILEGES;`.

### Paso 2.3: Poblado de Datos Iniciales y Catálogo
* **Script:** [`Farmacia/02_insert_data_farmacia_v1.sql`](Farmacia/02_insert_data_farmacia_v1.sql)
* **Usuario ejecutor:** `usuario_farmacia` o `root`
* **Acciones que realiza:**
  1. Desactiva temporalmente revisión de claves foráneas (`SET FOREIGN_KEY_CHECKS = 0;`).
  2. Limpia y pobla:
     * 5 Categorías farmacéuticas.
     * 5 Proveedores autorizados.
     * 20 Medicamentos para gripe, fiebre y dolores musculares.
     * 20 Lotes e inventarios con stock activo.
     * 3 Usuarios de farmacia con hash de contraseña **BCrypt oficial**.
     * 2 Prescripciones de prueba preparadas para orquestación SOA.
  3. Reactiva `SET FOREIGN_KEY_CHECKS = 1;`.

### Paso 2.4: Despliegue de los 50 Stored Procedures Oficiales
* **Script:** [`Farmacia/03_bd_farmacia_sp_v1.sql`](Farmacia/03_bd_farmacia_sp_v1.sql)
* **Usuario ejecutor:** `usuario_farmacia` o `root`
* **Acciones que realiza:**
  1. Configura `DELIMITER $$`.
  2. Despliega los **50 Stored Procedures validados** (5 operaciones por cada una de las 10 entidades: Registrar, Actualizar, Eliminar Lógico, Obtener por ID y Listar).
  3. Cada procedimiento incluye `DROP PROCEDURE IF EXISTS` para garantizar despliegue idempotente sin colisiones.
  4. Restaura `DELIMITER ;`.

*(Nota: El archivo `updatecontraseña.sql` NO debe ejecutarse, ya que sus hashes BCrypt se encuentran integrados en el Script 02).*

---

## 5. Tabla Resumen de Ejecución Secuencial Completa

| Orden | Archivo a Ejecutar | Base de Datos Destino | Usuario Ejecutor | Propósito Principal |
| :---: | :--- | :--- | :---: | :--- |
| **1** | `databaseMysql/v321/00_instalacion_completa_bd_citasmedicas_v321.sql` | `bd_citasmedicas_v321` | `root` | Crear BD Citas, tablas, datos y usuario `usuario_citas` |
| **2** | `databaseMysql/v321/01_stored_procedures_completos_v321.sql` | `bd_citasmedicas_v321` | `usuario_citas` o `root` | Desplegar 12 Stored Procedures de Citas y Médicos |
| **3** | `databaseMysql/Farmacia/00_bd_farmacia_v1.sql` | `bd_famacia` | `root` | Crear BD Farmacia, 10 tablas e índices |
| **4** | `databaseMysql/Farmacia/01_bd_usuario.sql` | `bd_famacia` | `root` | Crear usuario `'usuario_farmacia'@'localhost'` |
| **5** | `databaseMysql/Farmacia/02_insert_data_farmacia_v1.sql` | `bd_famacia` | `usuario_farmacia` o `root` | Poblar catálogo (20 fármacos, lotes, usuarios BCrypt) |
| **6** | `databaseMysql/Farmacia/03_bd_farmacia_sp_v1.sql` | `bd_famacia` | `usuario_farmacia` o `root` | Desplegar 50 Stored Procedures oficiales de Farmacia |

---

## 6. Ejecución Rápida por Línea de Comandos (CLI MySQL)

Si prefieres ejecutar el despliegue completo mediante consola (PowerShell / CMD), ejecuta los siguientes comandos en orden:

```bash
# ============================================================
# FASE 1: BASE DE DATOS CITAS
# ============================================================
mysql -u root -p < "databaseMysql/v321/00_instalacion_completa_bd_citasmedicas_v321.sql"
mysql -u usuario_citas -pISO/IEC27001 < "databaseMysql/v321/01_stored_procedures_completos_v321.sql"

# ============================================================
# FASE 2: BASE DE DATOS FARMACIA
# ============================================================
mysql -u root -p < "databaseMysql/Farmacia/00_bd_farmacia_v1.sql"
mysql -u root -p < "databaseMysql/Farmacia/01_bd_usuario.sql"
mysql -u usuario_farmacia -pFarmacia2026* < "databaseMysql/Farmacia/02_insert_data_farmacia_v1.sql"
mysql -u usuario_farmacia -pFarmacia2026* < "databaseMysql/Farmacia/03_bd_farmacia_sp_v1.sql"
```

---

## 7. Script de Verificación y Health Check Post-Despliegue

Ejecuta esta consulta en MySQL Workbench para validar que ambas bases de datos se encuentren 100% operativas:

```sql
-- 1. Verificar conteo de tablas y registros en Citas Médicas
USE bd_citasmedicas_v321;
SELECT 'bd_citasmedicas_v321' AS base_de_datos, 'paciente' AS tabla, COUNT(*) AS total_registros FROM paciente
UNION ALL SELECT 'bd_citasmedicas_v321', 'medico', COUNT(*) FROM medico
UNION ALL SELECT 'bd_citasmedicas_v321', 'cita', COUNT(*) FROM cita
UNION ALL SELECT 'bd_citasmedicas_v321', 'usuario', COUNT(*) FROM usuario;

-- 2. Verificar conteo de tablas y registros en Farmacia
USE bd_famacia;
SELECT 'bd_famacia' AS base_de_datos, 'medicamento' AS tabla, COUNT(*) AS total_registros FROM medicamento
UNION ALL SELECT 'bd_famacia', 'inventario', COUNT(*) FROM inventario
UNION ALL SELECT 'bd_famacia', 'prescripcion', COUNT(*) FROM prescripcion
UNION ALL SELECT 'bd_famacia', 'usuario_farmacia', COUNT(*) FROM usuario_farmacia;

-- 3. Verificar cantidad de procedimientos almacenados activos
SELECT ROUTINE_SCHEMA AS base_de_datos, COUNT(*) AS total_stored_procedures
FROM information_schema.ROUTINES
WHERE ROUTINE_SCHEMA IN ('bd_citasmedicas_v321', 'bd_famacia') AND ROUTINE_TYPE = 'PROCEDURE'
GROUP BY ROUTINE_SCHEMA;
```

**Resultado esperado del Health Check:**
* `bd_citasmedicas_v321`: **12 o más procedimientos almacenados**.
* `bd_famacia`: **Exactamente 50 procedimientos almacenados**.
