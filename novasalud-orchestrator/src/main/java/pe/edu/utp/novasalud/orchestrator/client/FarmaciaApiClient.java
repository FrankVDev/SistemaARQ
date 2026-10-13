package pe.edu.utp.novasalud.orchestrator.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pe.edu.utp.novasalud.orchestrator.config.ServiceUrlsConfig;
import pe.edu.utp.novasalud.orchestrator.dto.PrescripcionDto;
import pe.edu.utp.novasalud.orchestrator.util.AppLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class FarmaciaApiClient {

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public FarmaciaApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public JsonObject enviarPrescripcionAFarmacia(int idPaciente, int idAtencion, PrescripcionDto prescripcion, String jwtToken, String correlationId) {
        try {
            String url = ServiceUrlsConfig.getFarmaciaApiUrl() + "/prescripciones";

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("idPaciente", idPaciente);
            bodyMap.put("idAtencion", idAtencion);
            bodyMap.put("medicamentos", prescripcion.getMedicamentos());

            String jsonBody = gson.toJson(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                    .header("X-Correlation-ID", correlationId)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return gson.fromJson(response.body(), JsonObject.class);
            } else {
                AppLogger.error(FarmaciaApiClient.class, "Farmacia API retornó HTTP Status: " + response.statusCode(), null);
                return null;
            }
        } catch (Exception ex) {
            AppLogger.error(FarmaciaApiClient.class, "Error conectando con Farmacia API (Servicio posiblemente fuera de línea). Aplicando resiliencia.", ex);
            return null;
        }
    }
}
