package pe.edu.utp.farmacia.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.edu.utp.farmacia.model.Prescripcion;
import pe.edu.utp.farmacia.service.PrescripcionService;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/prescripciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PrescripcionResource {

    private final PrescripcionService service = new PrescripcionService();

    @GET
    public Response listar() {
        try {
            List<Prescripcion> lista = service.listar();
            return Response.ok(lista).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") int id) {
        try {
            Prescripcion p = service.obtenerPorId(id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Prescripción no encontrada"))
                        .build();
            }
            return Response.ok(p).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    public Response registrar(Prescripcion p, @QueryParam("usuario") @DefaultValue("sistema") String usuario) {
        try {
            int id = service.registrar(p, usuario);
            p.setIdPrescripcion(id);
            return Response.status(Response.Status.CREATED).entity(p).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        } catch (SQLException e) {
            if (esCodigoDuplicado(e)) {
                // La receta ya fue registrada antes (por ejemplo, en un reintento del ESB).
                // Se responde 409 Conflict para que el consumidor lo distinga de una falla real.
                Map<String, Object> error = new HashMap<>();
                error.put("error", "PRESCRIPCION_DUPLICADA");
                error.put("mensaje", "Ya existe una prescripción con el código indicado.");
                error.put("codigoPrescripcion", p.getCodigoPrescripcion());
                return Response.status(Response.Status.CONFLICT).entity(error).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", e.getMessage())).build();
        }
    }

    /**
     * Indica si el error corresponde a una clave única duplicada en MySQL
     * (ER_DUP_ENTRY, código 1062), es decir, un codigo_prescripcion repetido.
     * No se usa el SQLState 23000 porque también incluye otras violaciones
     * de integridad (por ejemplo, llaves foráneas), que no son duplicados.
     */
    private static boolean esCodigoDuplicado(SQLException e) {
        return e.getErrorCode() == 1062;
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") int id, Prescripcion p, @QueryParam("usuario") @DefaultValue("sistema") String usuario) {
        try {
            p.setIdPrescripcion(id);
            service.actualizar(p, usuario);
            return Response.ok(Map.of("mensaje", "Prescripción actualizada exitosamente")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id, @QueryParam("usuario") @DefaultValue("sistema") String usuario) {
        try {
            service.eliminar(id, usuario);
            return Response.ok(Map.of("mensaje", "Prescripción eliminada exitosamente")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
