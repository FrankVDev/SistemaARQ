package pe.edu.utp.novasalud.frontend.controller;

import pe.edu.utp.novasalud.frontend.model.AtencionMedica;
import pe.edu.utp.novasalud.frontend.model.Cita;
import pe.edu.utp.novasalud.frontend.util.ApiClient;
import pe.edu.utp.novasalud.frontend.util.AppLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Atención Médica para Frontend Web Java.
 * Invoca el Orquestador REST SOA (`novasalud-orchestrator`) o la API de Atención (`novasalud-atencion-api`).
 */
@WebServlet("/atencion-medica")
public class AtencionMedicaController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cargarFormulario(request, response, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        int idMedicoSesion = obtenerIdMedicoSesion(session);
        int idCita = parseEntero(request.getParameter("idCita"));
        String accion = texto(request.getParameter("accion"));

        try {
            validarDoctor(session);
            AtencionMedica atencion = leerAtencion(request);
            boolean marcarAtendida = "guardar_atender".equals(accion);

            if (marcarAtendida) {
                // Invocar al Orquestador REST SOA para ejecutar el flujo end-to-end (Cita -> Atención -> Farmacia)
                try {
                    Map<String, Object> reqOrquestador = new HashMap<>();
                    reqOrquestador.put("idCita", atencion.getIdCita());
                    reqOrquestador.put("idMedico", idMedicoSesion);
                    reqOrquestador.put("motivoConsulta", atencion.getMotivoConsulta());
                    reqOrquestador.put("sintomas", atencion.getSintomas());
                    reqOrquestador.put("diagnostico", atencion.getDiagnostico());
                    reqOrquestador.put("tratamiento", atencion.getTratamiento());
                    reqOrquestador.put("indicaciones", atencion.getIndicaciones());
                    reqOrquestador.put("recetaMedica", atencion.getRecetaMedica());

                    if (atencion.getRecetaMedica() != null && !atencion.getRecetaMedica().isBlank()) {
                        List<Map<String, Object>> medList = new ArrayList<>();
                        Map<String, Object> med = new HashMap<>();
                        med.put("nombre", atencion.getRecetaMedica());
                        med.put("dosis", "Según indicación médica");
                        med.put("frecuencia", "Cada 8 horas");
                        med.put("duracion", "3 días");
                        med.put("cantidad", 1);
                        medList.add(med);

                        Map<String, Object> presc = new HashMap<>();
                        presc.put("medicamentos", medList);
                        reqOrquestador.put("prescripcion", presc);
                    }

                    ApiClient.post("/orquestacion/atencion", reqOrquestador, Map.class);
                } catch (Exception exOrq) {
                    AppLogger.error(getClass(), "Orquestador no disponible, ejecutando fallback directo a novasalud-atencion-api", exOrq);
                    String url = "/atencion-medica?idMedicoSesion=" + idMedicoSesion + "&marcarAtendida=true";
                    ApiClient.post(url, atencion, AtencionMedica.class);
                }
                response.sendRedirect(request.getContextPath() + "/agenda-medico?buscar=1&fecha=" + texto(request.getParameter("fecha")) + "&msg=attended");
            } else {
                String url = "/atencion-medica?idMedicoSesion=" + idMedicoSesion + "&marcarAtendida=false";
                ApiClient.post(url, atencion, AtencionMedica.class);
                response.sendRedirect(request.getContextPath() + "/atencion-medica?idCita=" + idCita + "&msg=saved");
            }
        } catch (Exception ex) {
            AppLogger.error(getClass(), "Error al guardar atención médica vía REST API", ex);
            cargarFormulario(request, response, ex.getMessage(), leerAtencion(request));
        }
    }

    private void cargarFormulario(HttpServletRequest request, HttpServletResponse response, String error, AtencionMedica atencionTemporal)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int idCita = parseEntero(request.getParameter("idCita"));
        int idMedicoSesion = obtenerIdMedicoSesion(session);
        try {
            validarDoctor(session);
            Cita cita = ApiClient.get("/citas/" + idCita, Cita.class);
            if (cita == null || cita.getIdMedico() != idMedicoSesion) {
                response.sendRedirect(request.getContextPath() + "/acceso-denegado?from=atencion-medica");
                return;
            }

            AtencionMedica atencion = atencionTemporal;
            if (atencion == null) {
                try {
                    atencion = ApiClient.get("/atencion-medica/cita/" + idCita, AtencionMedica.class);
                } catch (Exception ignored) {}
            }

            if (atencion == null) {
                atencion = new AtencionMedica();
                atencion.setIdCita(cita.getIdCita());
                atencion.setIdMedico(cita.getIdMedico());
                atencion.setIdPaciente(cita.getIdPaciente());
                atencion.setCodigoHistoria(cita.getHistoriaClinicaCodigo());
            }

            request.setAttribute("cita", cita);
            request.setAttribute("atencion", atencion);
            if (error != null) request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/views/atencion/form.jsp").forward(request, response);
        } catch (Exception ex) {
            AppLogger.error(getClass(), "Error al cargar atención médica vía REST API", ex);
            response.sendRedirect(request.getContextPath() + "/acceso-denegado?from=atencion-medica");
        }
    }

    private AtencionMedica leerAtencion(HttpServletRequest request) {
        AtencionMedica atencion = new AtencionMedica();
        atencion.setIdCita(parseEntero(request.getParameter("idCita")));
        atencion.setMotivoConsulta(texto(request.getParameter("motivoConsulta")));
        atencion.setSintomas(texto(request.getParameter("sintomas")));
        atencion.setDiagnostico(texto(request.getParameter("diagnostico")));
        atencion.setTratamiento(texto(request.getParameter("tratamiento")));
        atencion.setRecetaMedica(texto(request.getParameter("recetaMedica")));
        atencion.setIndicaciones(texto(request.getParameter("indicaciones")));
        atencion.setCodigoHistoria(texto(request.getParameter("codigoHistoria")));
        return atencion;
    }

    private void validarDoctor(HttpSession session) {
        if (session == null || !"DOCTOR".equalsIgnoreCase(String.valueOf(session.getAttribute("rolUsuario")))) {
            throw new IllegalArgumentException("Solo el médico responsable puede registrar la atención clínica.");
        }
        if (obtenerIdMedicoSesion(session) <= 0) {
            throw new IllegalArgumentException("Su usuario no está vinculado a un médico real.");
        }
    }

    private int obtenerIdMedicoSesion(HttpSession session) {
        if (session == null || session.getAttribute("idMedico") == null) return 0;
        try { return Integer.parseInt(String.valueOf(session.getAttribute("idMedico"))); } catch (NumberFormatException ex) { return 0; }
    }

    private int parseEntero(String value) { try { return Integer.parseInt(value); } catch (Exception ex) { return 0; } }
    private String texto(String value) { return value == null ? "" : value.trim(); }
}
