package pe.edu.utp.novasalud.orchestrator.config;

import java.io.InputStream;
import java.util.Properties;

public class ServiceUrlsConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ServiceUrlsConfig.class.getClassLoader().getResourceAsStream("orchestrator.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception ignored) {}
    }

    public static String getCitasApiUrl() {
        String env = System.getenv("CITAS_API_URL");
        if (env != null && !env.isBlank()) return env.trim();
        return props.getProperty("citas.api.url", "http://localhost:8080/novasalud-citas-api/api");
    }

    public static String getAtencionApiUrl() {
        String env = System.getenv("ATENCION_API_URL");
        if (env != null && !env.isBlank()) return env.trim();
        return props.getProperty("atencion.api.url", "http://localhost:8082/novasalud-atencion-api/api");
    }

    public static String getFarmaciaApiUrl() {
        String env = System.getenv("FARMACIA_API_URL");
        if (env != null && !env.isBlank()) return env.trim();
        return props.getProperty("farmacia.api.url", "http://localhost:8081/novasalud-farmacia-api/api");
    }
}
