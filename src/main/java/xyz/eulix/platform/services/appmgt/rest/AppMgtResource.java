package xyz.eulix.platform.services.appmgt.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.appmgt.dto.*;
import xyz.eulix.platform.services.appmgt.service.AppMgtService;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * AppInfo管理类
 */
@ApplicationScoped
@Path("/v1/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform App Management Service",
        description = "Provides check app version related APIs.")
public class AppMgtResource {

    @Inject
    AppMgtService appMgtService;

    @GET
    @Path("/appinfo/check")
    @Logged
    @Operation(description = "Check app version for client.")
    public AppInfoCheckRes appInfoCheck(@NotBlank @HeaderParam("Request-Id") String requestId,
                                        @NotBlank @QueryParam("app_name") String appName,
                                        @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class, valueMethod = "getName")
                                        @QueryParam("app_type") String appType,
                                        @Pattern(regexp = "[0-9\\.]{0,20}") @Parameter(schema = @Schema(pattern = "[0-9\\.]{0,20}"),
                                                description = "当前版本") @QueryParam("cur_version") String curVersion) {
        return appMgtService.checkAppInfo(appName, appType, curVersion);
    }

    @POST
    @Path("/appinfo")
    @Logged
    @Operation(description = "Add app info.")
    public AppInfoRes appInfoSave(@NotBlank @HeaderParam("Request-Id") String requestId,
                                  @Valid AppInfoReq appInfoReq) {
        return appMgtService.saveAppinfo(appInfoReq);
    }

    @DELETE
    @Path("/appinfo")
    @Logged
    @Operation(description = "Delete app info.")
    public BaseResultRes appInfoDel(@NotBlank @HeaderParam("Request-Id") String requestId,
                                    @NotBlank @QueryParam("app_name") String appName,
                                    @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class, valueMethod = "getName")
                                        @QueryParam("app_type") String appType,
                                    @NotNull @Pattern(regexp = "[0-9\\.]{0,20}") @Parameter(schema = @Schema(pattern = "[0-9\\.]{0,20}"))
                                        @QueryParam("app_version") String appVersion) {
        appMgtService.delAppinfo(appName, appType, appVersion);
        return BaseResultRes.of(true);
    }
}
