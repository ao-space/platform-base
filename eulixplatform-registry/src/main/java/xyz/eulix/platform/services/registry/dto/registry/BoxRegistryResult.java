package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class BoxRegistryResult {
  @Schema(description = "盒子的注册码，用于后续平台对于盒子访问合法性的验证")
  private final String boxRegKey;

  @Schema(description = "为盒子分配的 network client 信息")
  private final NetworkClient networkClient;
}
