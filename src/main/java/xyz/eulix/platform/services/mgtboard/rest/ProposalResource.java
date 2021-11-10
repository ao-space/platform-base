package xyz.eulix.platform.services.mgtboard.rest;

import com.google.common.base.Stopwatch;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.service.ProposalService;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.serialization.OperationUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Proposal Rest类
 */
@ApplicationScoped
@Path("/v1/api")
@Tag(name = "Platform Proposal Management Service", description = "提供意见反馈相关接口.")
public class ProposalResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ProposalService proposalService;

    @Inject
    OperationUtils utils;

    @POST
    @Path("/proposal")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "新增意见反馈")
    public ProposalRes proposalSave(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                   @Valid ProposalReq proposalReq) {
        return proposalService.saveProposal(proposalReq);
    }

    @PUT
    @Path("/proposal/{proposal_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "更新意见反馈")
    public ProposalRes proposalUpdate(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                     @NotNull @Parameter(required = true) @PathParam("proposal_id") Long proposalId,
                                     @Valid ProposalReq proposalReq) {
        return proposalService.updateProposal(proposalId, proposalReq);
    }

    @DELETE
    @Path("/proposal/{proposal_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "删除意见反馈")
    public BaseResultRes proposalDel(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotNull @PathParam("proposal_id") Long proposalId) {
        proposalService.deleteProposal(proposalId);
        return BaseResultRes.of(true);
    }

    @GET
    @Path("/proposal/{proposal_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "查询意见反馈详情")
    public ProposalRes proposalGet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotNull @Parameter(required = true) @PathParam("proposal_id") Long proposalId) {
        return proposalService.getProposal(proposalId);
    }

    @GET
    @Path("/proposal/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "获取意见反馈列表")
    public PageListResult<ProposalRes> proposalList(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                    @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                                    @Parameter(required = true, description = "每页数量，最大1000") @Max(1000)
                                                        @QueryParam("page_size") Integer pageSize) {
        return proposalService.listProposal(currentPage, pageSize);
    }

    @POST
    @Path("/file/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "文件上传接口")
    public UploadFileRes upload(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                @Valid @MultipartForm MultipartBody multipartBody,
                                @QueryParam("a") String a) {
        LOG.infov("[Invoke] method: upload(), fileName: {0}, a:{1}", multipartBody.fileName, a);
        Stopwatch sw = Stopwatch.createStarted();
        UploadFileRes uploadFileRes;
        try {
            uploadFileRes = proposalService.upload(multipartBody);
        } catch (Exception e) {
            LOG.errorv(e,"[Throw] method: upload(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: upload(), result: {0}, elapsed: {1}", utils.objectToJson(uploadFileRes), sw);
        return uploadFileRes;
    }

    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/file/download")
    @Operation(description = "文件下载接口")
    public Response download(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                             @Valid DownloadFileReq downloadFileReq) {
        LOG.infov("[Invoke] method: download(), fileName: {0}", utils.objectToJson(downloadFileReq));
        Stopwatch sw = Stopwatch.createStarted();
        Response response;
        try {
            response = proposalService.download(downloadFileReq);
        } catch (Exception e) {
            LOG.errorv(e,"[Throw] method: download(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: download(), result: ok, elapsed: {0}", sw);
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/proposal/export")
    @Operation(description = "文件下载接口")
    public Response export(@Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
        LOG.infov("[Invoke] method: export()");
        var sw = Stopwatch.createStarted();
        Response response;
        try {
            response = proposalService.export();
        } catch (Exception e) {
            LOG.errorv(e,"[Throw] method: export(), exception");
            throw e;
        } finally {
            sw.stop();
        }
        LOG.infov("[Return] method: export(), result: ok, elapsed: {0}", sw);
        return response;
    }
}
