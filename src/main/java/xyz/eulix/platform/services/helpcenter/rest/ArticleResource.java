package xyz.eulix.platform.services.helpcenter.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import xyz.eulix.platform.services.helpcenter.dto.ArticleInfo;
import xyz.eulix.platform.services.helpcenter.service.ArticleService;
import xyz.eulix.platform.services.helpcenter.service.CatalogueService;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.mgtboard.dto.MultipartBody;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RequestScoped
@Path("/v1/api")
@Tag(name = "Article Service", description = "Provides article preset related APIs.")
public class ArticleResource {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  CatalogueService catalogueService;

  @Inject
  ArticleService articleService;

  @RolesAllowed("admin")
  @Logged
  @POST
  @Path("/catalogue/{path}/article")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "创建文章")
  public ArticleInfo createArticle(@PathParam("path") String path,
                                           @Valid @QueryParam("title") String title,
                                           @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    return articleService.createNewArticle(Long.valueOf(path),title, null);
  }

  @RolesAllowed("admin")
  @Logged
  @GET
  @Path("/catalogue/{path}/article")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "获取文章列表")
  public List<ArticleInfo> getArticles(@PathParam("path") String path,
                                       @Valid @QueryParam("title") String title,
                                       @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    return articleService.getArticleList(Long.valueOf(path));
  }

  @RolesAllowed("admin")
  @Logged
  @DELETE
  @Path("/catalogue/{path}/article/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "删除文章")
  public BaseResultRes deleteArticle(@PathParam("path") String path,
                                      @PathParam("path") Integer id,
                                      @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    return BaseResultRes.of(true);
  }

  @RolesAllowed("admin")
  @Logged
  @POST
  @Path("/catalogue/{path}/article/update/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "修改文章")
  public ArticleInfo updateArticle(@PathParam("path") String path,
                                      @PathParam("path") Integer id,
                                      @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    return articleService.updateArticle(Long.valueOf(path),id, null);
  }
}
