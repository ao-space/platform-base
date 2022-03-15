package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.service.ReservedDomainService;
import xyz.eulix.platform.services.support.log.Logged;
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
@Path("/v1/api")
@Tag(name = "Platform Reserved Domain Service", description = "提供域名管理接口.")
public class ReservedDomainResource {
    @Inject
    ReservedDomainService reservedDomainService;

    @RolesAllowed("admin")
    @Logged
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/domain/reserved")
    @Operation(description = "创建保留域名")
    public ReservedDomainCreateRsp create(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                             @Valid ReservedDomainCreateReq req) {
        return reservedDomainService.create(req);
    }

    @RolesAllowed("admin")
    @Logged
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/domain/reserved")
    @Operation(description = "修改保留域名")
    public ReservedDomainUpdateRsp update(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                           @Parameter(required = true, description = "正则id") @QueryParam("regex_id") Long regexId,
                           @Valid ReservedDomainUpdateReq req) {
        return reservedDomainService.update(regexId, req);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/domain/reserved")
    @Operation(description = "删除保留域名")
    public ReservedDomainDeleteRsp delete(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                          @Size(min = 1, max = 100) @Parameter(required = true, description = "正则id列表") @QueryParam("regex_ids") List<Long> regexIds) {
        return reservedDomainService.delete(regexIds);
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/domain/reserved")
    @Operation(description = "查询保留域名")
    public PageListResult<ReservedDomainInfo> queryReservedDomain(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                               @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                               @Parameter(required = true, description = "每页数量，最大1000") @Max(1000) @QueryParam("page_size") Integer pageSize) {
        return reservedDomainService.queryReservedDomain(currentPage, pageSize);
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/domain/reserved/match")
    @Operation(description = "查询某一条保留域名匹配的盒子域名")
    public List<ReservedDomainMatchInfo> queryMatchInfo(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                    @Parameter(required = true, description = "正则id") @QueryParam("regex_id") Long regexId) {
        return reservedDomainService.queryMatchInfo(regexId);
    }

}
