package xyz.eulix.platform.services.auth.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.auth.dto.GenPkeyRsp;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;
import xyz.eulix.platform.services.auth.entity.PkeyAuthEntity;
import xyz.eulix.platform.services.auth.service.AuthService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 鉴权类
 */
@ApplicationScoped
@Path("/v1/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Auth Service",
        description = "Provides client auth related APIs.")
public class AuthResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    AuthService authService;

    @GET
    @Path("/auth/pkey/gen")
    @Logged
    @Operation(description = "Generate pkey for new client.")
    public GenPkeyRsp pkeyGen(@NotBlank @HeaderParam("Request-Id") String requestId) {
        PkeyAuthEntity pkeyAuthEntity = authService.genPkey();
        return GenPkeyRsp.of(pkeyAuthEntity.getPkey(), pkeyAuthEntity.getExpiresAt());
    }

    @POST
    @Path("/auth/pkey/boxinfo")
    @Logged
    @Operation(description = "Receive box info from app(old client).")
    public void boxinfoTrans(@NotBlank @HeaderParam("Request-Id") String requestId,
                             @Valid TransBoxInfoReq boxInfoReq) {
        authService.savePkeyAuth(boxInfoReq);
    }

    @GET
    @Path("/auth/pkey/poll")
    @Logged
    @Operation(description = "Poll box info by new client.")
    public PollPkeyRsp pkeyPoll(@NotBlank @HeaderParam("Request-Id") String requestId,
                                @NotNull @Pattern(regexp = "[a-zA-Z0-9-]{36}")
                                @Parameter(schema = @Schema(type = SchemaType.STRING, pattern = "[a-zA-Z0-9-]{36}"))
                                @QueryParam("pkey") String pkey) {
        return authService.pollPkeyAuth(pkey);
    }
}
