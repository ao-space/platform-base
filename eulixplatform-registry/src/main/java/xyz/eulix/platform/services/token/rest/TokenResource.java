package xyz.eulix.platform.services.token.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.services.token.dto.TokenInfo;
import xyz.eulix.platform.services.token.dto.TokenResults;
import xyz.eulix.platform.services.token.service.TokenService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Token Service", description = "BoxRegKey管理")
public class TokenResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    TokenService tokenService;

    @Logged
    @POST
    @Path("/auth/box_reg_keys")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取box_reg_key")
    public TokenResults createTokens(@Valid TokenInfo tokenInfo,
                                    @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 验证签名
        var boxInfoEntity = tokenService.verifySign(tokenInfo);
        var tokenResults = tokenService.createBoxTokens(tokenInfo, boxInfoEntity);
        return TokenResults.of(tokenInfo.getBoxUUID(), tokenResults);
    }

}
