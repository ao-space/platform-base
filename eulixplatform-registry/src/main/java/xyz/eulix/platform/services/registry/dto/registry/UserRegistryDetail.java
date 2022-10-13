package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class UserRegistryDetail {
  @Schema(description = "为用户分配的用户域名，该域名可以用于后续的业务访问")
  private final String userDomain;

  @Schema(description = "用户的注册码，用于后续平台对于用户访问合法性的验证")
  private final String userRegKey;
}
