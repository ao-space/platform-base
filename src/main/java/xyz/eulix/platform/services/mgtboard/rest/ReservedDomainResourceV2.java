package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.dto.v2.ReservedDomainCreateRsp;
import xyz.eulix.platform.services.mgtboard.dto.v2.ReservedDomainUpdateRsp;
import xyz.eulix.platform.services.mgtboard.service.ReservedDomainService;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.BatchDeleteResult;
import xyz.eulix.platform.services.support.model.PageListResult;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@ApplicationScoped
@Path("/v2/service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Reserved Domain Service", description = "域名管理APIv2")
public class ReservedDomainResourceV2 {
    @Inject
    ReservedDomainService reservedDomainService;

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/domains")
    @Operation(description = "创建保留域名")
    public ReservedDomainCreateRsp create(@NotBlank @HeaderParam("Request-Id") String requestId,
                                          @Valid ReservedDomainCreateReq req) {
        return ReservedDomainCreateRsp.of();
    }

    @RolesAllowed("admin")
    @Logged
    @PUT
    @Path("/domains/{domain_id}")
    @Operation(description = "修改保留域名")
    public ReservedDomainUpdateRsp update(@NotBlank @HeaderParam("Request-Id") String requestId,
                                          @NotBlank @Parameter(required = true, description = "域名id") @PathParam("domain_id") Long domainId,
                                          @Valid ReservedDomainUpdateReq req) {
        return ReservedDomainUpdateRsp.of();
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/domains")
    @Operation(description = "批量删除保留域名")
    public BatchDeleteResult delete(@NotBlank @HeaderParam("Request-Id") String requestId,
                                    @Size(min = 1, max = 100) @Parameter(required = true, description = "正则id列表") @QueryParam("domain_ids") List<Long> domainIds) {
        return BatchDeleteResult.of();
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Path("/domains")
    @Operation(description = "查询保留域名列表")
    public PageListResult<ReservedDomainInfo> queryReservedDomain(@NotBlank @HeaderParam("Request-Id") String requestId,
                                                                  @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                                                  @Parameter(required = true, description = "每页数量，最大2000") @Max(2000) @QueryParam("page_size") Integer pageSize) {
        return reservedDomainService.queryReservedDomain(currentPage, pageSize);
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Path("/domains/{domain_id}/match")
    @Operation(description = "查询某一条保留域名匹配的盒子域名")
    public List<ReservedDomainMatchInfo> queryMatchInfo(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                        @NotBlank @Parameter(required = true, description = "域名id") @PathParam("domain_id") Long domainId) {
        return reservedDomainService.queryMatchInfo(domainId);
    }

}
