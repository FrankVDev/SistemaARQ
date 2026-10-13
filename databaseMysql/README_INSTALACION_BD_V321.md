# NovaSalud V3.2.1 - Bases de Datos (Citas Médicas y Farmacia)

Este repositorio contiene los scripts oficiales para el despliegue de las **dos bases de datos independientes** del sistema bajo la arquitectura SOA (*Database-per-Service*):

1. **`bd_citasmedicas_v321`**: Gestión clínica, médicos, pacientes, citas, atención y cobros.
2. **`bd_famacia`**: Gestión de catálogo farmacéutico, lotes, stock, prescripciones y ventas.

---

## 📖 Guía Unificada de Despliegue Paso a Paso

Para ver las instrucciones detalladas con el orden de ejecución exacto, credenciales y consultas de verificación, consulta el archivo:

👉 **[PASOS_DESPLIEGUE_BASES_DATOS.md](PASOS_DESPLIEGUE_BASES_DATOS.md)**

---

## ⚡ Resumen Rápido de Scripts y Orden de Ejecución

### 1. Base de Datos de Citas (`bd_citasmedicas_v321`):
1. `v321/00_instalacion_completa_bd_citasmedicas_v321.sql` *(Ejecutar como root)*
2. `v321/01_stored_procedures_completos_v321.sql` *(Ejecutar como usuario_citas o root)*

### 2. Base de Datos de Farmacia (`bd_famacia`):
1. `Farmacia/00_bd_farmacia_v1.sql` *(Ejecutar como root)*
2. `Farmacia/01_bd_usuario.sql` *(Ejecutar como root)*
3. `Farmacia/02_insert_data_farmacia_v1.sql` *(Ejecutar como usuario_farmacia o root)*
4. `Farmacia/03_bd_farmacia_sp_v1.sql` *(Ejecutar como usuario_farmacia o root)*

---

## 🔑 Credenciales de Acceso a MySQL

* **Usuario Citas:** `usuario_citas` / Contraseña: `ISO/IEC27001`
* **Usuario Farmacia:** `usuario_farmacia` / Contraseña: `Farmacia2026*`
* **Usuarios de prueba del sistema:** Clave por defecto `123456`

