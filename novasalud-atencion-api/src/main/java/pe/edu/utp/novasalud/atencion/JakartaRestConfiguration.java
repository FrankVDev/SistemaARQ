package pe.edu.utp.novasalud.atencion;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
@OpenAPIDefinition(
        info = @Info(
                title = "NovaSalud Atención Médica API",
                version = "3.2.1",
                description = "Servicio independiente de Atención Médica e Historial Clínico - NovaSalud SOA",
                contact = @Contact(name = "Equipo NovaSalud UTP", email = "contacto@novasalud.pe")
        ),
        security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese el token JWT obtenido desde POST /api/auth/login"
)
public class JakartaRestConfiguration extends Application {
}
