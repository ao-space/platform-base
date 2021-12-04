package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class RegistryResult {
  @Schema(description = "盒子的注册码，用于后续平台对于盒子访问合法性的验证")
  private final String boxRegKey;

  @Schema(description = "为用户分配的用户域名，该域名可以用于后续的业务访问")
  private final String userDomain;

  @Schema(description = "用户的注册码，用于后续平台对于用户访问合法性的验证")
  private final String userRegKey;

  @Schema(description = "客户端的注册码，用于后续平台对于客户端访问合法性的验证")
  private final String clientRegKey;

  @Schema(description = "为盒子分配的 network client 信息")
  private final NetworkClient networkClient;
}
