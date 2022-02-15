package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class BoxInfosRes {
  @Schema(description = "成功的盒子 UUID 列表")
  private final List<String> boxUUIDs;

  @Schema(description = "失败的盒子 UUID 列表")
  private final List<String> failures;
}
