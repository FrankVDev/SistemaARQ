package pe.edu.utp.novasalud.orchestrator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    public static void info(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).info(message);
    }

    public static void error(Class<?> clazz, String message, Throwable t) {
        LoggerFactory.getLogger(clazz).error(message, t);
    }
}
