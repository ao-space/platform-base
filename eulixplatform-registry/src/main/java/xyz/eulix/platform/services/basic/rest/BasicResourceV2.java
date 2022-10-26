package xyz.eulix.platform.services.basic.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.basic.dto.PlatformApi;
import xyz.eulix.platform.services.basic.dto.PlatformApiResults;
import xyz.eulix.platform.services.basic.dto.PlatformApiTypeEnum;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.common.support.model.StatusResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Basic Service", description = "基础APIv2.")
public class BasicResourceV2 {

    @Inject
    ApplicationProperties properties;

    @Logged
    @GET
    @Path("/ability")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询空间平台能力")
    public PlatformApiResults ability(@NotBlank @HeaderParam("Request-Id") String requestId) {
        List<PlatformApi> platformApis = new ArrayList<>();
        PlatformApi platformApi = PlatformApi.of("POST",
                "/platform/v*/api/registry/box",
                "/registry/box",
                Collections.singletonList(1),
                PlatformApiTypeEnum.BASIC_API.getName(),
                "注册盒子，成功后返回盒子的注册码，以及network client信息");
        platformApis.add(platformApi);
        return PlatformApiResults.of(platformApis);
    }

    @Logged
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询空间平台状态")
    public StatusResult status(@NotBlank @HeaderParam("Request-Id") String requestId) {
        return StatusResult.of("ok", properties.getVersion());
    }
}
