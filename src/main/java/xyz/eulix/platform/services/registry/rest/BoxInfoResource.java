package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.service.BoxInfoService;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.PageListResult;

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
import java.util.List;

@RequestScoped
@Path("/v1/api")
@Tag(name = "BoxInfo Preset Service", description = "Provides box info preset related APIs.")
public class BoxInfoResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    BoxInfoService boxInfoService;

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/boxinfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "预置盒子uuid，需管理员权限。")
    public BoxInfosRes saveBoxInfos(@Valid BoxInfosReq boxInfosReq,
                                    @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        return BoxInfosRes.of(boxInfoService.saveBoxInfos(boxInfosReq));
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
                                               @Parameter(required = true, description = "每页数量，最大1000") @Max(1000) @QueryParam("page_size") Integer pageSize) {
        return boxInfoService.listBoxInfo(currentPage, pageSize);
    }
}
