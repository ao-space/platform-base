package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.mgtboard.dto.CatalogueReq;
import xyz.eulix.platform.services.mgtboard.dto.CatalogueRes;
import xyz.eulix.platform.services.mgtboard.service.CatalogueService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RequestScoped
@Path("/v2/service")
@Tag(name = "Catalogue Service", description = "目录管理APIv2")
public class CatalogueResourceV2 {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    CatalogueService catalogueService;

    @Logged
    @GET
    @Path("/catalogues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询节点下所有子目录")
    public List<CatalogueRes> getCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                            @NotNull @Parameter(required = true) @QueryParam("parent_id") Long parentId) {
        return catalogueService.findByRootId(parentId);
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/catalogues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "创建目录")
    public CatalogueRes createCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                         @NotNull @Parameter(required = true) CatalogueReq catalogueReq) {
        return catalogueService.saveCatalogue(catalogueReq.getParentId(), catalogueReq.getCataName());
    }

    @RolesAllowed("admin")
    @Logged
    @PUT
    @Path("/catalogues/{cata_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "修改目录")
    public CatalogueRes updateCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                         @NotNull @Parameter(required = true) @PathParam("cata_id") Long cataId,
                                         @Valid CatalogueReq catalogueReq) {
        return catalogueService.updateCatalogue(cataId, catalogueReq.getCataName());

    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/catalogues/{cata_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除目录")
    public void deleteCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                 @NotNull @Parameter(required = true) @PathParam("cata_id") Long cataId) {
        catalogueService.deleteFromRootId(cataId);
    }
}
