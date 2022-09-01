package xyz.eulix.platform.services.basic.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.basic.dto.PlatformApi;
import xyz.eulix.platform.services.basic.dto.PlatformApiResults;
import xyz.eulix.platform.services.basic.dto.PlatformApiTypeEnum;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.StatusResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequestScoped
@Path("/platform")
@Tag(name = "Platform Basic Service", description = "Basic APIs.")
public class BasicResource {

    @Inject
    ApplicationProperties properties;
    @Logged
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Try to fetch the current status of server.")
    public StatusResult status() {
        return StatusResult.of("ok", properties.getVersion());
    }
}
