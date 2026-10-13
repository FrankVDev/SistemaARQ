package pe.edu.utp.novasalud.atencion.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionDB {

    private static final Properties FILE_PROPERTIES = new Properties();
    private static final HikariDataSource dataSource;

    static {
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                FILE_PROPERTIES.load(input);
            }
        } catch (IOException ex) {
            AppLogger.error(ConexionDB.class, "Error al leer el archivo db.properties", ex);
        }

        String driverClass = readProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError("No se encontró el driver JDBC: " + driverClass);
        }

        String url = readConfig("NOVASALUD_DB_URL", "DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/bd_citasmedicas_v321?useSSL=false&allowPublicKeyRetrieval=true&noAccessToProcedureBodies=true&serverTimezone=UTC");
        String user = readConfig("NOVASALUD_DB_USER", "DB_USER", "db.user", "usuario_citas");
        String password = readConfig("NOVASALUD_DB_PASSWORD", "DB_PASSWORD", "db.password", "ISO/IEC27001");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName(driverClass);

        config.setMaximumPoolSize(readIntProperty("db.pool.maximumPoolSize", 10));
        config.setMinimumIdle(readIntProperty("db.pool.minimumIdle", 2));
        config.setIdleTimeout(readLongProperty("db.pool.idleTimeout", 30000L));
        config.setConnectionTimeout(readLongProperty("db.pool.connectionTimeout", 10000L));
        config.setMaxLifetime(readLongProperty("db.pool.maxLifetime", 1800000L));
        config.setPoolName("NovaSaludAtencionHikariCP");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("noAccessToProcedureBodies", "true");

        dataSource = new HikariDataSource(config);
        AppLogger.info(ConexionDB.class, "Pool HikariCP inicializado para novasalud-atencion-api.");
    }

    private ConexionDB() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static Connection getConexion() throws SQLException {
        return getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static String readConfig(String specificEnvKey, String genericEnvKey, String propertyKey, String defaultValue) {
        String specificEnvValue = System.getenv(specificEnvKey);
        if (specificEnvValue != null && !specificEnvValue.isBlank()) return specificEnvValue.trim();

        String genericEnvValue = System.getenv(genericEnvKey);
        if (genericEnvValue != null && !genericEnvValue.isBlank()) return genericEnvValue.trim();

        String sysProperty = System.getProperty(propertyKey);
        if (sysProperty != null && !sysProperty.isBlank()) return sysProperty.trim();

        return readProperty(propertyKey, defaultValue);
    }

    private static String readProperty(String key, String defaultValue) {
        String val = FILE_PROPERTIES.getProperty(key);
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }

    private static int readIntProperty(String key, int defaultValue) {
        try {
            String val = readProperty(key, null);
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static long readLongProperty(String key, long defaultValue) {
        try {
            String val = readProperty(key, null);
            return val != null ? Long.parseLong(val) : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
