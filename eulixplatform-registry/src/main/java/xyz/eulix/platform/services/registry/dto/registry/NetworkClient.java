package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class NetworkClient {
  @Schema(description = "指定的客户端 ID")
  private final String clientId;

  @Schema(description = "指定的访问密钥")
  private final String secretKey;
}
