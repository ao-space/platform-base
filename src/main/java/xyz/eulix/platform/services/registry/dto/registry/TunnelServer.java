package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class TunnelServer {
  @Schema(description = "透传服务器地址")
  private final String address;

  @Schema(description = "透传服务器端口")
  private final Integer port;

  @Schema(description = "透传服务器访问认证信息")
  private final Auth auth;

  @Data(staticConstructor = "of")
  public static class Auth {
    @Schema(description = "指定的客户端 ID")
    private final String clientId;

    @Schema(description = "指定的访问密钥")
    private final String secretKey;
  }
}
