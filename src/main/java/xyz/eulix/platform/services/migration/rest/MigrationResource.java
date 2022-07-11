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
@Path("/v1/api")
@Tag(name = "Platform Migration Service", description = "Migration related APIs.")
public class MigrationResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Logged
    @POST
    @Path("migration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "平台割接接口，幂等设计")
    public BoxMigrationResult migration(@Valid BoxMigrationInfo boxMigrationInfo,
                                        @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        ClientMigrationResult clientMigrationResult = ClientMigrationResult.of("client_uuid",
                "client_reg_key");
        UserMigrationResult userMigrationResult = UserMigrationResult.of("user_id",
                "user_reg_key",
                "subdomain",
                RegistryTypeEnum.USER_ADMIN.getName(),
                Collections.singletonList(clientMigrationResult));
        return BoxMigrationResult.of("box_uuid",
                "box_reg_key",
                NetworkClient.of("network_client_id", "network_client_secret"),
                Collections.singletonList(userMigrationResult));
    }

    @Logged
    @POST
    @Path("/migration/route")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "域名重定向接口，幂等设计")
    public BaseResultRes migrationRoute(@Valid MigrationRouteInfo migrationRouteInfo,
                                        @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        return BaseResultRes.of(true);
    }
}
