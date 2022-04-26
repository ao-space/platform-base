package xyz.eulix.platform.services.applet.rest;

import com.google.common.base.Stopwatch;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.applet.dto.AppletInfoRes;
import xyz.eulix.platform.services.applet.dto.AppletPostReq;
import xyz.eulix.platform.services.applet.dto.AppletReq;
import xyz.eulix.platform.services.applet.service.AppletService;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/v1/api")
@Tag(name = "Applet Service", description = "Provides applet preset related APIs.")
public class AppletResource {
	private static final Logger LOG = Logger.getLogger("app.log");

	@Inject
	AppletService appletService;


	@Logged
	@GET
	@Path("/applet/info")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(description = "获取所有小程序信息")
	public List<AppletInfoRes> getAppletInfo(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId){
		return appletService.getAppletInfo();
	}

	//@RolesAllowed("admin")
	@Logged
	@POST
	@Path("/applet")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(description = "增加小程序信息")
	public AppletInfoRes createApplet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
									  @NotNull @Valid AppletPostReq appletPostReq){
		return appletService.saveApplet(appletPostReq);
	}

	//@RolesAllowed("admin")
	@Logged
	@PUT
	@Path("/applet/{applet_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(description = "更新小程序信息")
	public AppletInfoRes updateApplet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
									  @NotNull @Parameter(required = true) @PathParam("applet_id") String appletId,
									  @NotNull @Valid AppletPostReq appletReq){
		return  appletService.updateApplet(appletId, appletReq);
	}

	//@RolesAllowed("admin")
	@Logged
	@DELETE
	@Path("/applet/{applet_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(description = "删除小程序")
	public BaseResultRes delApplet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
									  @NotNull @Parameter(required = true) @PathParam("applet_id") String appletId){
		appletService.appletDelete(appletId);
		return BaseResultRes.of(true);
	}

	@Logged
	@POST
	@Path("/applet/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(description = "下载小程序")
	public Response downloadApplet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
								   @NotNull @Valid AppletReq appletReq){
		//检查小程序新版本是否与盒子版本兼容，以及请求的小程序版本是否最新
		appletService.compatiableCheck(appletReq);
		//开始下载并检测下载时常
		LOG.infov("[Invoke] method: appletDownload(), appletId: {0}", appletReq.getAppletId());
		Stopwatch sw = Stopwatch.createStarted();
		Response response;
		try {
			response = appletService.downAppletPackage(appletReq);
		} catch (Exception e) {
			LOG.errorv(e,"[Throw] method: download(), exception");
			throw e;
		} finally {
			sw.stop();
		}
		LOG.infov("[Return] method: appletDownload(), result: ok, elapsed: {0}", sw);
		return response;
	}
}
