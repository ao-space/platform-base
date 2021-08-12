package xyz.eulix.platform.services.auth.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.auth.dto.GenPkeyRsp;
import xyz.eulix.platform.services.auth.dto.PollPkeyReq;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;
import xyz.eulix.platform.services.auth.entity.BoxInfoEntity;
import xyz.eulix.platform.services.auth.service.AuthService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
    public GenPkeyRsp pkeyGen(@Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
        BoxInfoEntity boxInfoEntity = authService.genPkey();
        return GenPkeyRsp.of(boxInfoEntity.getPkey(), boxInfoEntity.getExpiresAt().toString());
    }

    @POST
    @Path("/auth/pkey/boxinfo")
    @Logged
    @Operation(description = "Receive box info from app(old client).")
    public void boxinfoTrans(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                             @Valid TransBoxInfoReq boxInfoReq) {
        authService.saveBoxInfo(boxInfoReq);
    }

    @GET
    @Path("auth/pkey/poll")
    @Logged
    @Operation(description = "Poll box info by new client.")
    public PollPkeyRsp pkeyPoll(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                @Valid PollPkeyReq pollPkeyReq) {
        return authService.pollBoxInfo(pollPkeyReq);
    }
}
