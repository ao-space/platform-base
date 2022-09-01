package xyz.eulix.platform.services.push.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.push.dto.BaseResultRes;
import xyz.eulix.platform.services.push.dto.DeviceTokenReq;
import xyz.eulix.platform.services.push.dto.DeviceTokenRes;
import xyz.eulix.platform.services.push.dto.PushMessage;
import xyz.eulix.platform.services.push.service.PushService;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.serialization.OperationUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/platform/v1/api")
@Tag(name = "Platform Push Service", description = "Provides message push related APIs.")
public class PushResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    PushService pushService;

    @Inject
    RegistryService registryService;

    @Inject
    OperationUtils operationUtils;

    @Logged
    @POST
    @Path("/push/device")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册 device token")
    public DeviceTokenRes registryDeviceToken(@Valid @HeaderParam("Request-Id") @NotBlank String requestId,
                                              @Valid DeviceTokenReq deviceTokenReq) {
        // 参数检验
        if (CommonUtils.isNotNull(deviceTokenReq.getExtra())) {
            try {
                operationUtils.jsonToObject(deviceTokenReq.getExtra(), Object.class);
            } catch (Exception e) {
                LOG.debugv("json format error, extra:{0}", deviceTokenReq.getExtra());
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "deviceTokenReq.extra");
            }
        }
        // 校验 Client 合法性
        registryService.hasClientNotRegisted(deviceTokenReq.getBoxUUID(), deviceTokenReq.getUserId(), deviceTokenReq.getClientUUID(),
                deviceTokenReq.getClientRegKey());
        DeviceTokenRes deviceTokenRes = pushService.registryDeviceToken(deviceTokenReq);
        return deviceTokenRes;
    }

    @Logged
    @POST
    @Path("/push/message")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "发送通知")
    public BaseResultRes pushMessage(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                     @Valid PushMessage pushMessage) {
        Boolean result = pushService.pushMessage(pushMessage);
        return BaseResultRes.of(result);
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/broadcast/message")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "广播通知")
    public BaseResultRes broadcastMessage(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                     @Valid PushMessage pushMessage) {
        Boolean result = pushService.broadcastMessage(pushMessage);
        return BaseResultRes.of(result);
    }
}
