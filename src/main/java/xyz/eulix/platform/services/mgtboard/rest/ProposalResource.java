package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Proposal Rest类
 */
@ApplicationScoped
@Path("/v1/api")
@Tag(name = "Platform Proposal Management Service", description = "提供意见反馈相关接口.")
public class ProposalResource {
    @POST
    @Path("/proposal")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "新增意见反馈")
    public ProposalRes proposalSave(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                   @Valid ProposalReq proposalReq) {
        return ProposalRes.of(null, null, null, null, null);
    }

    @PUT
    @Path("/proposal/{proposal_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "更新意见反馈")
    public ProposalRes proposalUpdate(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                     @NotBlank @Parameter(required = true) @PathParam("proposal_id") String proposalId,
                                     @Valid ProposalReq proposalReq) {
        return ProposalRes.of(null, null, null, null, null);
    }

    @DELETE
    @Path("/proposal/{proposal_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "删除意见反馈")
    public BaseResultRes proposalDel(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotBlank @PathParam("proposal_id") String proposalId) {
        return BaseResultRes.of(true);
    }

    @GET
    @Path("/proposal/{proposal_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "查询意见反馈详情")
    public ProposalRes proposalGet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotBlank @Parameter(required = true) @PathParam("proposal_id") String proposalId) {
        return ProposalRes.of(null, null, null, null, null);
    }

    @GET
    @Path("/proposal/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "获取意见反馈列表")
    public List<ProposalRes> proposalList(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                          @Parameter(required = true) @QueryParam("current_page") Integer currentPage,
                                          @Parameter(required = true) @Max(1000) @QueryParam("page_size") Integer pageSize) {
        return new ArrayList<>();
    }

    @POST
    @Path("/file/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "文件上传接口")
    public UploadFileRes upload(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                   @MultipartForm MultipartBody proposalReq) {
        return UploadFileRes.of(null, null, null, null);
    }

    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/file/download")
    @Logged
    @Operation(description = "文件下载接口")
    public Response download(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                             @Valid DownloadFileReq downloadFileReq) {
        return Response.ok().build();
    }
}
