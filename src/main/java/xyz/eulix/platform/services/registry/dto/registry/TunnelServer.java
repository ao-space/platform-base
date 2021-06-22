package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;

@Data(staticConstructor = "of")
public class TunnelServer {
  private final String address;
  private final Integer port;
  private final Auth auth;

  @Data(staticConstructor = "of")
  public static class Auth {
    private final String clientId;
    private final String secretKey;
  }
}
