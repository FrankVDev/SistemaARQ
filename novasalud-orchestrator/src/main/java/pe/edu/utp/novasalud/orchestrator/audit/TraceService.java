package pe.edu.utp.novasalud.orchestrator.audit;

import pe.edu.utp.novasalud.orchestrator.util.AppLogger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class TraceService {

    private static final AtomicLong counter = new AtomicLong(1);

    public static String generateCorrelationId() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = counter.getAndIncrement() % 1000000;
        return String.format("CORR-%s-%06d", fecha, seq);
    }

    public static void logStep(String correlationId, String step, String service, String status, String details) {
        String logMsg = String.format("[%s] STEP: %s | SERVICE: %s | STATUS: %s | DETAILS: %s",
                correlationId, step, service, status, details);
        AppLogger.info(TraceService.class, logMsg);
    }
}
