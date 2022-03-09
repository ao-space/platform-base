package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Data(staticConstructor = "of")
public class ArticleReq {
    @NotBlank
    @Schema(required = true, description = "标题")
    private String title;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "目录id")
    private Long cataId;

    @Schema(description = "是否发布")
    private Boolean isPublish;
}
