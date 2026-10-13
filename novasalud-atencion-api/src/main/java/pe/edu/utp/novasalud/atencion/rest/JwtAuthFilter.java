package pe.edu.utp.novasalud.atencion.rest;

import io.jsonwebtoken.Claims;
import pe.edu.utp.novasalud.atencion.util.JwtUtil;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // Exclusión para Swagger y pre-flight OPTIONS
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS") || path.contains("openapi") || path.contains("swagger")) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Se requiere autenticación JWT para acceder a este servicio.\"}")
                    .build());
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Claims claims = JwtUtil.validarToken(token);
        if (claims == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token JWT inválido o expirado.\"}")
                    .build());
            return;
        }

        requestContext.setProperty("jwt.claims", claims);
    }
}
