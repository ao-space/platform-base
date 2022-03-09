package xyz.eulix.platform.services.helpcenter.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class ArticleContentInfo {
  @Schema(description = "文章内容")
  private String content;
}
