package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class BoxFailureInfo {
  @Schema(description = "行号")
  private final String rowNum;


  @Schema(description = "boxUUID")
  private final String boxUUID;
}

