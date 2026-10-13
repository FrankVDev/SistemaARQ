package pe.edu.utp.novasalud.orchestrator.dto;

public class AtencionProcessResponse {

    private boolean success;
    private String correlationId;
    private String proceso;
    private int idCita;
    private int idAtencion;
    private int idPaciente;
    private int idMedico;
    private boolean prescripcionGenerada;
    private Integer farmaciaOrdenId;
    private String estadoFarmacia;
    private String estado;
    private String mensaje;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getProceso() { return proceso; }
    public void setProceso(String proceso) { this.proceso = proceso; }

    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }

    public int getIdAtencion() { return idAtencion; }
    public void setIdAtencion(int idAtencion) { this.idAtencion = idAtencion; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

    public boolean isPrescripcionGenerada() { return prescripcionGenerada; }
    public void setPrescripcionGenerada(boolean prescripcionGenerada) { this.prescripcionGenerada = prescripcionGenerada; }

    public Integer getFarmaciaOrdenId() { return farmaciaOrdenId; }
    public void setFarmaciaOrdenId(Integer farmaciaOrdenId) { this.farmaciaOrdenId = farmaciaOrdenId; }

    public String getEstadoFarmacia() { return estadoFarmacia; }
    public void setEstadoFarmacia(String estadoFarmacia) { this.estadoFarmacia = estadoFarmacia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
