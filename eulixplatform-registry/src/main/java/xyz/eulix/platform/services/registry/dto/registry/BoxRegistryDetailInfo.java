package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Data(staticConstructor = "of")
public class BoxRegistryDetailInfo {
  @Schema(description = "为盒子分配的 network client 信息")
  private final String networkClientId;

  @Schema(description = "创建时间")
  private final OffsetDateTime createdAt;

  @Schema(description = "修改时间")
  private final OffsetDateTime updatedAt;

  @Schema(description = "盒子下用户信息")
  private final List<UserRegistryDetailInfo> userInfos;
}
