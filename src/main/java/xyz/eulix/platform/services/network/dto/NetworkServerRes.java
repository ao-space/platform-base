package xyz.eulix.platform.services.network.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class NetworkServerRes {
  @Schema(description = "透传服务器地址")
  private final String serverAddress;
}
