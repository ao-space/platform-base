package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.service.AppMgtService;
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
 * Package Rest类
 */
@ApplicationScoped
@Path("/v1/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform App Management Service", description = "提供App、Box版本管理接口")
public class PackageResource {

    @Inject
    AppMgtService appMgtService;

    @GET
    @Path("/package/compatible")
    @Logged
    @Operation(description = "检查app、box版本兼容性")
    public CompatibleCheckRes pkgCompatibleCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                       @NotBlank @Parameter(required = true) @QueryParam("bundle_id") String bundleId,
                                       @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class, valueMethod = "getName")
                                           @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"}))
                                           @QueryParam("platform") String platform,
                                       @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxPkgName,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {
        return CompatibleCheckRes.of(null, null);
    }

    @GET
    @Path("/package/app/check")
    @Logged
    @Operation(description = "检查app版本更新")
    public PackageCheckRes appPkgCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                        @NotBlank @Parameter(required = true) @QueryParam("bundle_id") String bundleId,
                                        @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class, valueMethod = "getName")
                                            @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"}))
                                            @QueryParam("platform") String platform,
                                        @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                        @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {
        return PackageCheckRes.of(null, null);
    }

    @POST
    @Path("/package/app")
    @Logged
    @Operation(description = "增加app版本")
    public AppPkgRes appPkgSave(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                @Valid AppPkgReq appPkgReq) {
        return appMgtService.saveAppinfo(appPkgReq);
    }

    @PUT
    @Path("/package/app")
    @Logged
    @Operation(description = "更新app版本")
    public AppPkgRes appPkgUpdate(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                  @Valid AppPkgReq appPkgReq) {
        return appMgtService.updateAppinfo(appPkgReq);
    }

    @DELETE
    @Path("/package/app")
    @Logged
    @Operation(description = "删除app版本")
    public BaseResultRes appPkgDel(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotBlank @Parameter(required = true) @QueryParam("bundle_id") String bundleId,
                                    @NotNull @ValueOfEnum(enumClass = AppTypeEnum.class, valueMethod = "getName")
                                        @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"})) 
                                        @QueryParam("platform") String platform,
                                    @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("app_pkg_version") String appPkgVersion) {
        appMgtService.delAppinfo(bundleId, platform, appPkgVersion);
        return BaseResultRes.of(true);
    }

    @GET
    @Path("/package/box/check")
    @Logged
    @Operation(description = "检查box版本更新")
    public PackageCheckRes boxPkgCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                       @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxPkgName,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {
        return PackageCheckRes.of(null, null);
    }

    @POST
    @Path("/package/box")
    @Logged
    @Operation(description = "增加box版本")
    public BoxPkgRes boxPkgSave(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                @Valid BoxPkgReq boxPkgReq) {
        return BoxPkgRes.of(null,null,null,null,null,null,false,null);
    }

    @PUT
    @Path("/package/box")
    @Logged
    @Operation(description = "更新box版本")
    public BoxPkgRes boxPkgUpdate(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                  @Valid BoxPkgReq boxPkgReq) {
        return BoxPkgRes.of(null,null,null,null,null,null,false,null);
    }

    @DELETE
    @Path("/package/box")
    @Logged
    @Operation(description = "删除box版本")
    public BaseResultRes boxPkgDel(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                   @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxPkgName,
                                   @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("box_pkg_version") String boxPkgVersion) {
        return BaseResultRes.of(true);
    }
}
