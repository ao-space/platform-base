package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data(staticConstructor = "of")
public class ArticleReq {
    @NotNull
    @Size(max = 10)
    @Schema(required = true, description = "标题")
    private String title;

    @Schema(description = "文章内容")
    @Size(max = 20000)
    private String content;

    @Schema(description = "目录id")
    private Long cataId;

    @Schema(description = "是否发布", enumeration = {"0", "1"})
    private Integer isPublish;
}
