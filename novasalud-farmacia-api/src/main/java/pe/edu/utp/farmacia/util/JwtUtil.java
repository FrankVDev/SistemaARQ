package pe.edu.utp.farmacia.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utilitario para la generación y validación de tokens JWT (HMAC-SHA256).
 *
 * La clave secreta es compartida por todos los servicios backend de NovaSalud
 * (Citas, Atención, Farmacia y Orquestador), de modo que un token emitido por
 * un sistema sea reconocido por los demás durante los procesos de integración.
 *
 * Orden de carga de la clave:
 * 1. Variable de entorno NOVASALUD_JWT_SECRET (Docker / producción)
 * 2. Propiedad jwt.secret del archivo db.properties
 * 3. Valor por defecto compartido
 */
public final class JwtUtil {

    private static final String DEFAULT_SECRET = "NovaSaludSystemSecretKeyHMAC256BitsLongForSecureJwtTokenSigning2026UTP";
    private static final long EXPIRATION_TIME = 86400000L; // 24 horas

    private static final Key SIGNING_KEY;

    static {
        byte[] keyBytes = loadSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        SIGNING_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    private JwtUtil() {
    }

    public static String generarToken(String username, String rol, int idUsuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        claims.put("idUsuario", idUsuario);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String obtenerUsername(String token) {
        return obtenerClaims(token).getSubject();
    }

    public static boolean esTokenValido(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private static String loadSecret() {
        String envSecret = System.getenv("NOVASALUD_JWT_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            return envSecret.trim();
        }

        try (InputStream input = JwtUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                String val = props.getProperty("jwt.secret");
                if (val != null && !val.isBlank()) {
                    return val.trim();
                }
            }
        } catch (Exception ignored) {
        }

        return DEFAULT_SECRET;
    }
}
