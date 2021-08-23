package xyz.eulix.platform.services.appmgt.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.appmgt.dto.AppInfoReq;
import xyz.eulix.platform.services.appmgt.dto.AppInfoRes;
import xyz.eulix.platform.services.appmgt.dto.AppTypeEnum;
import xyz.eulix.platform.services.appmgt.dto.BaseResultRes;
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
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    AppMgtService appMgtService;

    @GET
    @Path("/appinfo/check")
    @Logged
    @Operation(description = "Check app version for client.")
    public AppInfoRes appInfoCheck(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                   @Valid @NotBlank @QueryParam("app_name") String appName,
                                   @Valid @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class)
                                       @QueryParam("app_type") AppTypeEnum appType,
                                   @Valid @Pattern(regexp = "[0-9\\.]{0,20}") @Parameter(schema = @Schema(pattern = "[0-9\\.]{0,20}"))
                                       @QueryParam("cur_version") String curVersion) {
        return null;
    }

    @POST
    @Path("/appinfo")
    @Logged
    @Operation(description = "Add app info.")
    public AppInfoRes appinfoAdd(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                 @Valid AppInfoReq appInfoReq) {
        return null;
    }

    @DELETE
    @Path("/appinfo")
    @Logged
    @Operation(description = "Delete app info.")
    public BaseResultRes appinfoDel(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                    @Valid @NotBlank @QueryParam("app_name") String appName,
                                    @Valid @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class)
                                        @QueryParam("app_type") AppTypeEnum appType,
                                    @Valid @Pattern(regexp = "[0-9\\.]{0,20}") @Parameter(schema = @Schema(pattern = "[0-9\\.]{0,20}"))
                                        @QueryParam("app_version") String appVersion) {
        return null;
    }
}
