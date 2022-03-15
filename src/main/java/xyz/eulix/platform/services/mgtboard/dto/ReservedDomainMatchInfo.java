package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class ReservedDomainMatchInfo {
  @Schema(description = "全局唯一的 subdomain")
  private final String subdomain;

  @Schema(description = "更新时间")
  private final OffsetDateTime updatedAt;
}