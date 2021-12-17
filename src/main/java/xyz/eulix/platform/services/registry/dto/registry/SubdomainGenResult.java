package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class SubdomainGenResult {
  @Schema(description = "盒子的 UUID")
  private final String boxUUID;

  @Schema(description = "全局唯一的 subdomain")
  private final String subdomain;
}
