package xyz.eulix.platform.services.network.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.network.dto.BaseResultRes;
import xyz.eulix.platform.services.network.dto.NetworkAuthReq;
import xyz.eulix.platform.services.network.dto.NetworkServerRes;
import xyz.eulix.platform.services.network.dto.StunServerRes;
import xyz.eulix.platform.services.network.service.NetworkService;
import xyz.eulix.platform.common.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/platform/v1/api")
@Tag(name = "Platform Network Manage Service", description = "Provides network manage related APIs.")
public class NetworkResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    NetworkService networkService;

    @Logged
    @POST
    @Path("/network/client/auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "认证 network client 身份")
    public BaseResultRes networkClientAuth(@Valid NetworkAuthReq networkAuthReq,
                                           @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        Boolean result = networkService.networkClientAuth(networkAuthReq);
        return BaseResultRes.of(result);
    }

    @Logged
    @GET
    @Path("/network/server/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询最新 network server 信息")
    public NetworkServerRes networkServerDetail(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                @NotBlank @Parameter(required = true) @QueryParam("network_client_id") String networkClientId) {
        return networkService.networkServerDetail(networkClientId);
    }

    @Logged
    @GET
    @Path("/stun/server/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询相应 stun server 信息")
    public StunServerRes stunServerDetail(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                          @NotBlank @Parameter(required = true) @QueryParam("subdomain") String subdomain) {
        return networkService.stunServerDetail(subdomain);
    }
}
