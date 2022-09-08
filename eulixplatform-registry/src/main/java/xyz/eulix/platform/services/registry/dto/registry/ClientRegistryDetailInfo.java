package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class ClientRegistryDetailInfo {
  @Schema(description = "注册类型")
  private final String userType;

  @Schema(description = "客户端UUID")
  private final String clientUUID;

  @Schema(description = "创建时间")
  private final OffsetDateTime createdAt;
}
