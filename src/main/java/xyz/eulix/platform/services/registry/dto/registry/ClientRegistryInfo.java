package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class ClientRegistryInfo {
  @NotBlank
  @Schema(description = "盒子的 UUID")
  private String boxUUID;

  @NotBlank
  @Schema(description = "用户的 ID")
  private String userId;

  @NotBlank
  @Schema(description = "用户的注册码")
  private String userRegKey;

  @NotBlank
  @Schema(description = "客户端的 UUID")
  private String clientUUID;

  private String subdomain;

  @NotBlank
  @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth")
  @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
  private String clientType;
}
