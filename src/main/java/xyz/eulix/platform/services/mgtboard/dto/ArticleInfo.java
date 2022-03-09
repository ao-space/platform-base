package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class ArticleInfo {
  @Schema(description = "文章id")
  private final Long id;

  @Schema(description = "文章标题")
  private final String title;

  @Schema(description = "文章所在目录id")
  private final Long cataId;

  @Schema(description = "文章状态发布/草稿")
  private final int state;

  @Schema(description = "文章最后一次发布时间")
  private final OffsetDateTime publishdAt;

  @Schema(description = "文章创建时间")
  private final OffsetDateTime createAt;

  @Schema(description = "文章上一次更新时间")
  private final OffsetDateTime updateAt;
}
