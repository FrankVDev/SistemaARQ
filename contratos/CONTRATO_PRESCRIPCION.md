# Contrato de integración: Prescripción médica (Atención → Farmacia)

**Versión:** 1.0  
**Fase:** 2 – Contrato de la prescripción  
**Participantes:** Orquestador, ESB, Farmacia API, Citas API, Atención API, Farmacia Web

Este documento define los mensajes que intercambian los servicios cuando una
atención médica genera una receta, y cómo el ESB los transforma y enriquece.
Es la referencia para implementar las Fases 3 y 4 (ESB y transformación).

---

## 1. Visión general

Hay **dos integraciones** entre los sistemas de Citas y Farmacia:

| # | Integración | Dirección | Momento | Patrón |
| --- | --- | --- | --- | --- |
| 1 | **Envío de la prescripción** | Atención → Farmacia | Al finalizar la atención | Transformación de mensajes (*Message Translator*) |
| 2 | **Consulta de datos del paciente y médico** | Farmacia → Citas / Atención | Al mostrar la receta en Farmacia | Enriquecimiento de contenido (*Content Enricher*) |

```
Integración 1 (escritura)
Orquestador ──(prescripción canónica)──► ESB ──(transformación)──► Farmacia API ──► BD Farmacia

Integración 2 (lectura)
Farmacia Web ──► ESB ──► Farmacia API      (receta: solo IDs externos)
                  ├────► Citas API         (nombre y DNI del paciente, nombre y CMP del médico)
                  └────► Atención API      (diagnóstico)
                  ◄── receta enriquecida
```

Los patrones *Message Translator* y *Content Enricher* corresponden al catálogo
de Hohpe y Woolf, *Enterprise Integration Patterns* (2003).

---

## 2. Integración 1: Envío de la prescripción

### 2.1 Mensaje canónico (Orquestador → ESB)

Formato neutral, independiente de cómo Farmacia almacena los datos.

- **Endpoint (ESB):** `POST /esb/farmacia/prescripciones`
- **Formato:** JSON
- **Encabezados:** `Authorization: Bearer <token>`, `X-Correlation-ID: <id>`
- **Esquema:** [`prescripcion-canonica.schema.json`](prescripcion-canonica.schema.json)
- **Ejemplo:** [`ejemplos/01_prescripcion_canonica.json`](ejemplos/01_prescripcion_canonica.json)

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `idAtencion` | entero | Sí | ID de la atención registrada en Atención API |
| `idCita` | entero | Sí | ID de la cita en Citas API |
| `idPaciente` | entero | Sí | ID del paciente en Citas API |
| `idMedico` | entero | Sí | ID del médico en Citas API |
| `indicacionesGenerales` | texto (700) | No | Indicaciones generales de la receta |
| `medicamentos[]` | lista | Sí (mínimo 1) | Medicamentos prescritos |
| `medicamentos[].nombre` | texto (150) | Sí | Nombre comercial o genérico y concentración |
| `medicamentos[].cantidad` | entero ≥ 1 | Sí | Unidades a dispensar |
| `medicamentos[].dosis` | texto (100) | No | Ej. "1 tableta" |
| `medicamentos[].frecuencia` | texto (100) | No | Ej. "Cada 8 horas" |
| `medicamentos[].duracion` | texto (100) | No | Ej. "3 días" |
| `medicamentos[].indicaciones` | texto (500) | No | Indicaciones específicas del medicamento |

### 2.2 Formato de Farmacia (ESB → Farmacia API)

- **Endpoint:** `POST /novasalud-farmacia-api/api/prescripciones`
- **Esquema:** [`prescripcion-farmacia.schema.json`](prescripcion-farmacia.schema.json)
- **Ejemplo:** [`ejemplos/02_prescripcion_farmacia.json`](ejemplos/02_prescripcion_farmacia.json)

### 2.3 Reglas de transformación (las aplica el ESB)

| Campo Farmacia | Origen en el mensaje canónico | Regla |
| --- | --- | --- |
| `codigoPrescripcion` | `idAtencion` | `"RX-" + idAtencion` (único en BD Farmacia) |
| `idPacienteExterno` | `idPaciente` | Copia directa |
| `idMedicoExterno` | `idMedico` | Copia directa |
| `idAtencionExterna` | `idAtencion` | Copia directa |
| `instruccionesGenerales` | `indicacionesGenerales` | Copia directa |
| `estado` | — | Valor fijo `"PENDIENTE"` |
| `fechaPrescripcion` | — | No se envía; la BD asigna la fecha actual |
| `detalles[].nombreMedicamento` | `medicamentos[].nombre` | Copia directa |
| `detalles[].cantidad` | `medicamentos[].cantidad` | Copia directa |
| `detalles[].dosis` / `frecuencia` / `duracion` / `indicaciones` | mismo nombre | Copia directa |
| `detalles[].idMedicamento` | — | `null` (el químico lo asocia al catálogo al dispensar) |

### 2.4 Respuestas

| Situación | Farmacia API | Respuesta del ESB al Orquestador | Estado que registra el Orquestador |
| --- | --- | --- | --- |
| Receta registrada | `201 Created` + `idPrescripcion` | `201` + `{ idPrescripcion, codigoPrescripcion }` | `ENVIADO_A_FARMACIA` |
| Receta ya registrada antes (mismo `RX-{idAtencion}`) | Error por código duplicado | `200` + `{ codigoPrescripcion, duplicado: true }` | `ENVIADO_A_FARMACIA` |
| Mensaje inválido (sin medicamentos, sin nombre o cantidad) | — (el ESB no lo envía) | `400` + detalle de la validación | Error del proceso |
| Token inválido | `401` | `401` | Error del proceso |
| Farmacia no disponible (tras los reintentos del ESB) | Timeout / `5xx` | `503` | `PENDIENTE_ENVIO_FARMACIA` |

---

## 3. Integración 2: Consulta de la receta enriquecida (Opción 1)

### 3.1 Decisión

Farmacia **no almacena** datos personales del paciente ni del médico: solo guarda
los identificadores externos (`idPacienteExterno`, `idMedicoExterno`,
`idAtencionExterna`). Cuando Farmacia Web muestra una receta, el **ESB consulta
esos datos a los sistemas que son dueños de la información** y los agrega a la
respuesta.

**Motivos de la decisión:**

- **Sin duplicidad de datos:** el nombre y el DNI del paciente siguen teniendo una
  sola fuente (Citas). Si se corrigen allí, Farmacia ve el dato actualizado.
- **Autonomía de los dominios:** cada sistema conserva la responsabilidad sobre su
  información, tal como plantea la arquitectura SOA del documento.
- **Menor exposición de datos personales:** la BD de Farmacia no guarda copias de
  información sensible del paciente.
- **Aprovecha lo existente:** Farmacia Web ya tiene en su pantalla las columnas
  *Médico, CMP, Paciente, DNI y Diagnóstico*, que hoy aparecen vacías.

**Alternativa descartada:** enviar el nombre y el DNI dentro del texto de las
indicaciones. Es más simple, pero duplica datos personales y los mezcla con
información clínica en un campo de texto libre.

### 3.2 Contrato

- **Endpoints (ESB):** `GET /esb/farmacia/prescripciones` y `GET /esb/farmacia/prescripciones/{id}`
- **Consumidor:** Farmacia Web
- **Ejemplo de respuesta:** [`ejemplos/03_prescripcion_enriquecida.json`](ejemplos/03_prescripcion_enriquecida.json)

| Campo agregado | Servicio consultado por el ESB | Operación existente |
| --- | --- | --- |
| `nombrePaciente`, `dniPaciente` | Citas API | `GET /api/pacientes/{idPacienteExterno}` |
| `nombreMedico`, `numeroColegiaturaMedico` | Citas API | `GET /api/medicos/{idMedicoExterno}` |
| `diagnostico` | Atención API | `GET /api/atencion-medica/{idAtencionExterna}` |

### 3.3 Reglas

- Si un servicio consultado no responde, el ESB **devuelve la receta igual**, con
  esos campos vacíos. La dispensación no debe bloquearse por una consulta
  complementaria.
- Las recetas registradas manualmente en Farmacia (sin IDs externos) se devuelven
  sin enriquecer.
- La consulta usa el token del usuario de Farmacia (la clave JWT es compartida
  desde la Fase 1).

---

## 4. Reglas comunes

- **Trazabilidad:** todos los mensajes llevan `X-Correlation-ID`. Si una
  solicitud llega sin él, el ESB lo genera.
- **Seguridad:** todos los mensajes llevan `Authorization: Bearer <token>`.
- **Codificación:** UTF-8.

---

## 5. Estados de la prescripción en Farmacia

Los estados reales del sistema son los que maneja la BD y Farmacia Web:

| Estado | Significado |
| --- | --- |
| `PENDIENTE` | Receta recibida, pendiente de dispensar |
| `ATENDIDA` | Receta dispensada |

> El documento APF2 (sección 9.3) menciona `RECIBIDO / EN_PREPARACION / DESPACHADO / CANCELADO`.
> Se recomienda actualizar el documento a los estados reales del sistema.

Estados del envío, registrados por el Orquestador (no por Farmacia):
`ENVIADO_A_FARMACIA`, `PENDIENTE_ENVIO_FARMACIA`, `NO_APLICA`.
