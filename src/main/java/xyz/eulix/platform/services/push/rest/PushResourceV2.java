package xyz.eulix.platform.services.push.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.push.dto.BaseResultRes;
import xyz.eulix.platform.services.push.dto.v2.DeviceTokenReq;
import xyz.eulix.platform.services.push.dto.DeviceTokenRes;
import xyz.eulix.platform.services.push.dto.v2.PushMessage;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Push Service", description = "消息推送APIv2.")
public class PushResourceV2 {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Logged
    @POST
    @Path("/device/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册 device token")
    public DeviceTokenRes registryDeviceToken(@Valid DeviceTokenReq deviceTokenReq,
                                              @HeaderParam("Request-Id") @NotBlank String reqId,
                                              @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey) {
        return null;
    }

    @Logged
    @POST
    @Path("/message/push")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "发送通知")
    public BaseResultRes pushMessage(@Valid PushMessage pushMessage,
                                     @HeaderParam("Request-Id") @NotBlank String reqId,
                                     @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey) {
        return BaseResultRes.of(true);
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/message/broadcast")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "广播通知")
    public BaseResultRes broadcastMessage(@Valid PushMessage pushMessage,
                                          @HeaderParam("Request-Id") @NotBlank String reqId) {
        return BaseResultRes.of(true);
    }
}
