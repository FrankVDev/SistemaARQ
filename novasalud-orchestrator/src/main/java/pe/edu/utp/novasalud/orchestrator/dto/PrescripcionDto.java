package pe.edu.utp.novasalud.orchestrator.dto;

import java.util.List;

public class PrescripcionDto {

    private List<MedicamentoItemDto> medicamentos;

    public List<MedicamentoItemDto> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<MedicamentoItemDto> medicamentos) { this.medicamentos = medicamentos; }
}
