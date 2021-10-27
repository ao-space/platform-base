package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.service.PkgMgtService;
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
@Tag(name = "Platform Package Management Service", description = "提供App、Box版本管理接口")
public class PackageResource {

    @Inject
    PkgMgtService pkgMgtService;

    @GET
    @Path("/package/compatible")
    @Logged
    @Operation(description = "检查app、box版本兼容性")
    public CompatibleCheckRes pkgCompatibleCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                       @NotBlank @Parameter(required = true) @QueryParam("app_pkg_name") String appPkgName,
                                       @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                           @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"}))
                                           @QueryParam("app_pkg_type") String appPkgType,
                                       @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxPkgName,
                                       @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                           @Parameter(required = true, schema = @Schema(enumeration = {"box"}))
                                           @QueryParam("box_pkg_type") String boxPkgType,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {
        return CompatibleCheckRes.of(null, null);
    }

    @GET
    @Path("/package/check")
    @Logged
    @Operation(description = "检查软件包版本更新")
    public PackageCheckRes appPkgCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                       @NotBlank @Parameter(required = true, schema = @Schema(enumeration = {"app_check", "box_check"}))
                                           @ValueOfEnum(enumClass = PkgActionEnum.class, valueMethod = "getName") @QueryParam("action") String action,
                                       @NotBlank @Parameter(required = true) @QueryParam("pkg_name") String pkgName,
                                       @NotNull @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios", "box"}))
                                           @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName") @QueryParam("pkg_type") String pkgType,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                       @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {
      return pkgMgtService.checkPkgInfo(action, pkgName, pkgType, curBoxVersion, curAppVersion);
    }

    @POST
    @Path("/package")
    @Logged
    @Operation(description = "增加软件包版本")
    public PackageRes appPkgSave(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                 @Valid PackageReq packageReq) {
        return pkgMgtService.savePkgInfo(packageReq);
    }

    @PUT
    @Path("/package")
    @Logged
    @Operation(description = "更新软件包版本")
    public PackageRes appPkgUpdate(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                   @Valid PackageReq packageReq) {
        return pkgMgtService.updateAppinfo(packageReq);
    }

    @DELETE
    @Path("/package")
    @Logged
    @Operation(description = "删除软件包版本")
    public BaseResultRes appPkgDel(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotBlank @Parameter(required = true) @QueryParam("pkg_name") String pkgName,
                                    @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                        @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios", "box"}))
                                        @QueryParam("pkg_type") String pkgType,
                                    @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("pkg_version") String pkgVersion) {
        pkgMgtService.delAppinfo(pkgName, pkgType, pkgVersion);
        return BaseResultRes.of(true);
    }
}
