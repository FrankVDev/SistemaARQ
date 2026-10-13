package pe.edu.utp.novasalud.orchestrator.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pe.edu.utp.novasalud.orchestrator.config.ServiceUrlsConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CitasApiClient {

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public CitasApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public JsonObject obtenerCitaPorId(int idCita, String jwtToken, String correlationId) throws Exception {
        String url = ServiceUrlsConfig.getCitasApiUrl() + "/citas/" + idCita;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                .header("X-Correlation-ID", correlationId)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            return null;
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al consultar Citas API. HTTP Status: " + response.statusCode() + " - Body: " + response.body());
        }
        return gson.fromJson(response.body(), JsonObject.class);
    }
}
