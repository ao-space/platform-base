package xyz.eulix.platform.services;

import org.eclipse.microprofile.openapi.annotations.Operation;
import xyz.eulix.platform.services.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.StatusResult;
import xyz.eulix.platform.services.support.log.Logged;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/status")
public class StatusResource {

    @Inject
    ApplicationProperties properties;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "Try to fetch the current status of server.")
    public StatusResult status() {
        return StatusResult.of("ok", properties.getVersion());
    }
}
