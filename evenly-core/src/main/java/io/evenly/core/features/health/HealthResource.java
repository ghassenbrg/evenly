package io.evenly.core.features.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class HealthResource {

    @GET
    @Path("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
