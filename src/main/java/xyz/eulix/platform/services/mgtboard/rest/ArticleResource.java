package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.ArticleInfo;
import xyz.eulix.platform.services.mgtboard.dto.ArticleReq;
import xyz.eulix.platform.services.mgtboard.dto.ArticleRes;
import xyz.eulix.platform.services.mgtboard.service.ArticleService;
import xyz.eulix.platform.services.mgtboard.service.CatalogueService;
import xyz.eulix.platform.services.mgtboard.dto.BaseResultRes;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.PageListResult;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
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
    @Path("/article")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "创建文章")
    public ArticleRes createArticle(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @Valid ArticleReq articleReq) {
//        return articleService.createNewArticle(Long.valueOf(path),title, null);
        return ArticleRes.of(null, null, null, null, null, null, null, null);
    }

    @Logged
    @GET
    @Path("/article/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取文章列表")
    public PageListResult<ArticleRes> getArticles(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                  @NotBlank @Parameter(required = true) @QueryParam("cata_id") String cataId,
                                                  @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                                  @Parameter(required = true, description = "每页数量，最大1000") @Max(1000) @QueryParam("page_size") Integer pageSize) {
//        return articleService.getArticleList(Long.valueOf(path));
        return PageListResult.of(null, null);
    }

    @Logged
    @GET
    @Path("/article/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取文章列表")
    public ArticleRes getArticleDetail(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                  @NotBlank @Parameter(required = true) @QueryParam("cata_id") String cataId) {
        return ArticleRes.of(null, null, "123", null, null, null, null, null);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/article/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除文章")
    public BaseResultRes deleteArticle(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                       @NotNull @Parameter(required = true) @PathParam("id") Long articleId) {
        return BaseResultRes.of(true);
    }

    @RolesAllowed("admin")
    @Logged
    @PUT
    @Path("/article/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "修改文章")
    public ArticleRes updateArticle(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotNull @Parameter(required = true) @PathParam("id") Long articleId,
                                    @Valid ArticleReq articleReq) {
//        return articleService.updateArticle(Long.valueOf(path),id, null);
        return ArticleRes.of(null, null, null, null, null, null, null, null);
    }
}
