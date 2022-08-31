package xyz.eulix.platform.services.applet.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.applet.dto.*;
import xyz.eulix.platform.services.applet.dto.v2.AppletReq;
import xyz.eulix.platform.services.applet.service.AppletService;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/v2/service")
@Tag(name = "Applet Service", description = "小程序APIv2")
public class AppletResourceV2 {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    AppletService appletService;


    @Logged
    @GET
    @Path("/applets")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询小程序列表")
    public List<AppletInfoRes> getApplets(@NotBlank @HeaderParam("Request-Id") String requestId) {
        return appletService.getAppletInfo();
    }

    @Logged
    @GET
    @Path("/applets/{applet_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询小程序详情")
    public List<AppletInfoRes> getAppletDetail(@NotBlank @HeaderParam("Request-Id") String requestId,
                                               @NotBlank @QueryParam("applet_id") String appletId) {
        return appletService.getAppletInfo(appletId);
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/applets")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "增加小程序信息")
    public AppletRegistryRes registryApplet(@NotBlank @HeaderParam("Request-Id") String requestId,
                                            @Valid AppletRegistryInfo appletRegistryInfo) {
        return appletService.saveApplet(appletRegistryInfo);
    }

    @RolesAllowed("admin")
    @Logged
    @PUT
    @Path("/applets/{applet_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "更新小程序信息")
    public AppletInfoRes updateApplet(@NotBlank @HeaderParam("Request-Id") String requestId,
                                      @NotBlank @PathParam("applet_id") String appletId,
                                      @Valid AppletPostReq appletReq) {
        return appletService.updateApplet(appletId, appletReq);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/applets/{applet_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除小程序")
    public BaseResultRes delApplet(@NotBlank @HeaderParam("Request-Id") String requestId,
                                   @NotBlank @PathParam("applet_id") String appletId) {
        appletService.appletDelete(appletId);
        return BaseResultRes.of(true);
    }

    @Logged
    @POST
    @Path("/applets/{applet_id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "下载小程序")
    public Response downloadApplet(@NotBlank @HeaderParam("Request-Id") String requestId,
                                   @NotBlank @PathParam("applet_id") String appletId,
                                   @Valid AppletReq appletReq) {
        return null;
    }

    @Logged
    @GET
    @Path("/applets/{applet_id}/secret/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "校验小程序密钥")
    public CheckAppletResult checkAppletResult(@NotBlank @HeaderParam("Request-Id") String requestId,
                                               @NotBlank @HeaderParam("Box-Reg-Key") String boxRegKey,
                                               @NotBlank @PathParam("applet_id") String appletId,
                                               @NotBlank @QueryParam("box_uuid") String boxUUID,
                                               @NotBlank @QueryParam("applet_secret") String appletSecret) {
        return appletService.checkApplet(boxUUID, boxRegKey, appletId, appletSecret);
    }
}
