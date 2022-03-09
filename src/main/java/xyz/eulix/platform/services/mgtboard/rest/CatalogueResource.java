package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.CatalogueReq;
import xyz.eulix.platform.services.mgtboard.dto.PackageReq;
import xyz.eulix.platform.services.mgtboard.dto.QatalogueRes;
import xyz.eulix.platform.services.mgtboard.entity.CatalogueEntity;
import xyz.eulix.platform.services.mgtboard.service.CatalogueService;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/v1/api")
@Tag(name = "Catalogue Service", description = "Provides catalogue preset related APIs.")
public class CatalogueResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    CatalogueService catalogueService;

    @Logged
    @GET
    @Path("/catalogue/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询节点下所有子目录")
    public List<QatalogueRes> getCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                            @NotBlank @Parameter(required = true) @QueryParam("id") String id) {
//        String[] argsPath = path.split("/");
//        catalogueService.judePath(argsPath);
//        return catalogueService.findByRootId(Long.valueOf(argsPath[argsPath.length-1]));
        return new ArrayList<>();
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/catalogue")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "创建目录")
    public QatalogueRes createCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                         @Valid CatalogueReq catalogueReq) {
//    String[] argsPath = path.split("/");
//    catalogueService.judePath(argsPath);
//    return catalogueService.saveCatalogue(Long.valueOf(argsPath[argsPath.length-1]), pathName);
        return QatalogueRes.of(null, null, null, null, null);
    }

    @RolesAllowed("admin")
    @Logged
    @PUT
    @Path("/catalogue/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "修改目录")
    public QatalogueRes updateCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                         @NotNull @Parameter(required = true) @PathParam("id") Long cataId,
                                         @Valid CatalogueReq catalogueReq) {
//        String[] argsPath = path.split("/");
//        catalogueService.judePath(argsPath);
//        return catalogueService.updateCatalogue(Long.valueOf(argsPath[argsPath.length-1]), pathName);
        return QatalogueRes.of(null, null, null, null, null);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/catalogue/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除目录")
    public BaseResultRes deleteCatalogues(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                          @NotNull @Parameter(required = true) @PathParam("id") Long cataId) {
//        String[] argsPath = path.split("/");
//        catalogueService.judePath(argsPath);
//        catalogueService.deleteFromRootId(Long.valueOf(argsPath[argsPath.length-1]));
        return BaseResultRes.of(true);
    }
}
