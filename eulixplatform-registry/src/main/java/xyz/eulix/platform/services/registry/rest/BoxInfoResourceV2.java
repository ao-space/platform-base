package xyz.eulix.platform.services.registry.rest;

import com.google.common.base.Stopwatch;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import xyz.eulix.platform.services.registry.dto.registry.BaseResultRes;
import xyz.eulix.platform.services.registry.dto.registry.MultipartBody;
import xyz.eulix.platform.services.registry.dto.registry.BoxInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxInfosReq;
import xyz.eulix.platform.services.registry.dto.registry.BoxInfosRes;
import xyz.eulix.platform.services.registry.service.BoxInfoService;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.common.support.model.PageListResult;
import xyz.eulix.platform.common.support.serialization.OperationUtils;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "BoxInfo Preset Service", description = "盒子出场信息管理APIv2")
public class BoxInfoResourceV2 {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    BoxInfoService boxInfoService;

    @Inject
    OperationUtils utils;

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/boxinfos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "预置盒子出厂信息，需管理员权限")
    public BoxInfosRes saveBoxInfos(@Valid BoxInfosReq boxInfosReq,
                                    @HeaderParam("Request-Id") @NotBlank String reqId) {
        return boxInfoService.saveBoxInfos(boxInfosReq);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/boxinfos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "批量删除盒子出厂信息，需管理员权限")
    public BaseResultRes delBoxInfos(@NotBlank @HeaderParam("Request-Id") String requestId,
                                     @Valid @Size(min = 1, max = 2000) @QueryParam("box_uuids") List<@NotBlank String> boxUUIDs) {
        boxInfoService.delBoxInfos(boxUUIDs);
        return BaseResultRes.of(true);
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Path("/boxinfos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询盒子出厂信息列表，需管理员权限")
    public PageListResult<BoxInfo> boxInfoList(@NotBlank @HeaderParam("Request-Id") String requestId,
                                               @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                               @Parameter(required = true, description = "每页数量，最大2000") @Max(2000) @QueryParam("page_size") Integer pageSize,
                                               @Parameter(description = "是否注册") @QueryParam("isregistry") Boolean isRegistry,
                                               @Parameter(description = "boxuuid") @QueryParam("boxuuid") String boxUUID,
                                               @Parameter(description = "cpuid") @QueryParam("cpuid") String cpuId) {
        if (CommonUtils.isNotNull(isRegistry)) {
            return boxInfoService.listBoxInfo(currentPage, pageSize, isRegistry);
        } else if (CommonUtils.isNotNull(boxUUID)) {
            return boxInfoService.findBoxByBoxUUID(boxUUID);
        } else if (CommonUtils.isNotNull(cpuId)) {
            return boxInfoService.findBoxByBoxUUID(utils.string2SHA256("eulixspace-productid-" + cpuId));
        }
        return boxInfoService.listBoxInfo(currentPage, pageSize);
    }

    @RolesAllowed("admin")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/boxinfos/template")
    @Operation(description = "模板下载")
    public Response tempalte(@NotBlank @HeaderParam("Request-Id") String requestId) {
        LOG.infov("[Invoke] method: template()");
        Stopwatch sw = Stopwatch.createStarted();
        Response response;
        try {
            response = boxInfoService.template();
        } catch (Exception e) {
            LOG.errorv(e, "[Throw] method: template(), exception");
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
    @Path("/boxinfos/upload")
    @Operation(description = "盒子出厂信息导入")
    public BoxInfosRes upload(@NotBlank @HeaderParam("Request-Id") String requestId,
                              @Valid @MultipartForm MultipartBody multipartBody) {
        LOG.infov("[Invoke] method: upload(), fileName: {0}", multipartBody.fileName);
        Stopwatch sw = Stopwatch.createStarted();
        BoxInfosRes uploadRes;
        try {
            uploadRes = boxInfoService.upload(multipartBody);
        } catch (Exception e) {
            LOG.errorv(e, "[Throw] method: upload(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: upload(), result: {0}, elapsed: {0}", utils.objectToJson(uploadRes), sw);
        return uploadRes;
    }

    @RolesAllowed("admin")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/boxinfos/export")
    @Operation(description = "盒子出厂信息导出")
    public Response export(@NotBlank @HeaderParam("Request-Id") String requestId,
                           @Valid @Size(min = 1, max = 1000) @QueryParam("box_uuids") List<@NotBlank String> boxUUIDs) {
        return null;
    }
}
