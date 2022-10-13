package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class ClientRegistryDetail {
  @Schema(description = "客户端的注册码，用于后续平台对于客户端访问合法性的验证")
  private final String clientRegKey;
}
