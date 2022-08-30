package xyz.eulix.platform.services.migration.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.migration.dto.*;
import xyz.eulix.platform.services.push.dto.BaseResultRes;
import xyz.eulix.platform.services.registry.dto.registry.NetworkClient;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Migration Service", description = "割接APIv2.")
public class MigrationResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Logged
    @POST
    @Path("boxes/{box_uuid}/migration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "空间平台割接")
    public BoxMigrationResult migration(@Valid BoxMigrationInfo boxMigrationInfo,
                                        @HeaderParam("Request-Id") @NotBlank String reqId,
                                        @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                        @PathParam("box_uuid") @NotBlank String boxUUID) {
        return BoxMigrationResult.of();
    }

    @Logged
    @POST
    @Path("boxes/{box_uuid}/route")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "域名重定向")
    public MigrationRouteResult migrationRoute(@Valid MigrationRouteInfo migrationRouteInfo,
                                               @HeaderParam("Request-Id") @NotBlank String reqId,
                                               @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                               @PathParam("box_uuid") @NotBlank String boxUUID) {
        return MigrationRouteResult.of();
    }
}
