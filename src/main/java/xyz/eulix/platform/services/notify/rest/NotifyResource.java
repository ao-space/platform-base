package xyz.eulix.platform.services.notify.rest;


import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.notify.dto.NotifyDeviceInfo;
import xyz.eulix.platform.services.notify.dto.NotifyMessageInfo;
import xyz.eulix.platform.services.notify.dto.NotifyResult;
import xyz.eulix.platform.services.notify.entity.NotifyDevice;
import xyz.eulix.platform.services.notify.entity.NotifyMessage;
import xyz.eulix.platform.services.notify.service.NotifyDeviceService;
import xyz.eulix.platform.services.notify.service.NotifyMessageService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RequestScoped
@Path("v1/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notify Resource",
        description = "Provides Notify related APIs.")
public class NotifyResource {
    @Inject
    NotifyMessageService messageService;

    @Inject
    NotifyDeviceService deviceService;

    @POST
    @Path("/push/message")
    @Logged
    @Operation(description = "Create a new push message.")
    public NotifyResult<String> pushMessage(@Valid NotifyMessageInfo info) {
        NotifyMessage message = messageService.createMessage(info);
        List<NotifyDevice> devices = deviceService.activeDevicesByClientUUID(message.getClientUUID());
        messageService.pushMessage(message, devices);
        return NotifyResult.of(0, message.getMessageId());
    }

    @POST
    @Path("/register/device")
    @Logged
    @Operation(description = "Register a device.")
    public NotifyResult<String> registerDevice(@Valid NotifyDeviceInfo device) {
        return NotifyResult.of(0, deviceService.registerDevice(device).getDeviceId());
    }
}
