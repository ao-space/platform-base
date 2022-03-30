package xyz.eulix.platform.services.registry.rest;

import com.google.common.base.Stopwatch;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.mgtboard.dto.MultipartBody;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.service.BoxInfoService;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.serialization.OperationUtils;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RequestScoped
@Path("/v1/api")
@Tag(name = "BoxInfo Preset Service", description = "Provides box info preset related APIs.")
public class BoxInfoResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    BoxInfoService boxInfoService;

    @Inject
    OperationUtils utils;

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/boxinfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "预置盒子uuid，需管理员权限。")
    public BoxInfosRes saveBoxInfos(@Valid BoxInfosReq boxInfosReq,
                                    @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        return boxInfoService.saveBoxInfos(boxInfosReq);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/boxinfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除盒子uuid，需管理员权限")
    public BaseResultRes delBoxInfos(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                     @Size(min = 1, max = 1000) @QueryParam("box_uuids") List<@NotBlank String> boxUUIDs) {
        boxInfoService.delBoxInfos(boxUUIDs);
        return BaseResultRes.of(true);
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Path("/boxinfo/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询盒子uuid列表，需管理员权限。")
    public PageListResult<BoxInfo> boxInfoList(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                               @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                               @Parameter(required = true, description = "每页数量，最大2000") @Max(2000) @QueryParam("page_size") Integer pageSize,
                                               @Parameter(description = "是否注册") @QueryParam("isregistry") Boolean isRegistry,
                                               @Parameter(description = "boxuuid") @QueryParam("boxuuid") String boxUUID,
                                               @Parameter(description = "cpuid") @QueryParam("cpuid") String cpuId) {
        if(CommonUtils.isNotNull(isRegistry)){
            return boxInfoService.listBoxInfo(currentPage, pageSize, isRegistry);
        }else if(CommonUtils.isNotNull(boxUUID)){
            return boxInfoService.findBoxByBoxUUID(boxUUID);
        }else if(CommonUtils.isNotNull(cpuId)){
            return boxInfoService.findBoxByBoxUUID(utils.string2SHA256("eulixspace-productid-" + cpuId));
        }
        return boxInfoService.listBoxInfo(currentPage, pageSize);
    }

    @RolesAllowed("admin")
    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/boxinfo/template")
    @Operation(description = "模板下载接口")
    public Response tempalte(@Valid @NotBlank @HeaderParam("Request-Id") String requestId){
        LOG.infov("[Invoke] method: template()");
        Stopwatch sw = Stopwatch.createStarted();
        Response response;
        try {
            response = boxInfoService.template();
        } catch (Exception e) {
            LOG.errorv(e,"[Throw] method: template(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: template(), result: ok, elapsed: {0}", sw);
        return response;
    }

    @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/boxinfo/upload")
    @Operation(description = "设备信息导入接口")
    public BoxInfosRes upload(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                              @Valid @MultipartForm MultipartBody multipartBody) {
        LOG.infov("[Invoke] method: upload(), fileName: {0}", multipartBody.fileName);
        Stopwatch sw = Stopwatch.createStarted();
        BoxInfosRes uploadRes;
        try {
            uploadRes = boxInfoService.upload(multipartBody);
        } catch (Exception e) {
            LOG.errorv(e,"[Throw] method: upload(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: upload(), result: {0}, elapsed: {0}", utils.objectToJson(uploadRes), sw);
        return uploadRes;
    }

    @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/boxinfo/export")
    @Operation(description = "设备信息导出接口")
    public Response export(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                           @Valid @NotNull  BoxInfosReq boxInfosReq) {
        LOG.infov("[Invoke] method: export()");
        Stopwatch sw = Stopwatch.createStarted();
        Response response;
        try {
            response = boxInfoService.export(boxInfosReq.getBoxInfos());
        } catch (Exception e) {
            LOG.errorv(e,"[Throw] method: export(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: export(),elapsed: {0}", sw);
        return response;
    }
}
