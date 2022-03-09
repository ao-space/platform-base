package xyz.eulix.platform.services.helpcenter.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.helpcenter.entity.CatalogueEntity;
import xyz.eulix.platform.services.helpcenter.service.CatalogueService;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
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
@Path("/v1/api")
@Tag(name = "Catalogue Service", description = "Provides catalogue preset related APIs.")
public class CatalogueResource {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  CatalogueService catalogueService;

  @RolesAllowed("admin")
  @Logged
  @GET
  @Path("/catalogue/{path}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "查询节点下所有子目录")
  public List<CatalogueEntity> getCatalogues(@PathParam("path") String path,
                                                 @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    String[] argsPath = path.split("/");
    catalogueService.judePath(argsPath);
    return catalogueService.findByRootId(Long.valueOf(argsPath[argsPath.length-1]));
  }

  @RolesAllowed("admin")
  @Logged
  @POST
  @Path("/catalogue/create/{path}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "创建子目录")
  public CatalogueEntity createCatalogues(@PathParam("path") String path,
                                          @NotNull @QueryParam("catalogue_name") @Schema(description = "目录名字") String pathName,
                                          @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    String[] argsPath = path.split("/");
    catalogueService.judePath(argsPath);
    return catalogueService.saveCatalogue(Long.valueOf(argsPath[argsPath.length-1]), pathName);
  }

  @RolesAllowed("admin")
  @Logged
  @POST
  @Path("/catalogue/update/{path}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "修改子目录")
  public CatalogueEntity updateCatalogues(@PathParam("path") String path,
                                          @NotNull  @QueryParam("catalogue_name") @Schema(description = "目录名字") String pathName,
                                          @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    String[] argsPath = path.split("/");
    catalogueService.judePath(argsPath);
    return catalogueService.updateCatalogue(Long.valueOf(argsPath[argsPath.length-1]), pathName);
  }

  @RolesAllowed("admin")
  @Logged
  @DELETE
  @Path("/catalogue/delete/{path}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "删除子目录")
  public BaseResultRes deleteCatalogues(@PathParam("path") String path,
                                               @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    String[] argsPath = path.split("/");
    catalogueService.judePath(argsPath);
    catalogueService.deleteFromRootId(Long.valueOf(argsPath[argsPath.length-1]));
    return BaseResultRes.of(true);
  }
}
