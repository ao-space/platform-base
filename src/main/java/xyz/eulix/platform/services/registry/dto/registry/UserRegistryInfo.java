package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.mgtboard.dto.PkgTypeEnum;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class UserRegistryInfo {
  @NotBlank
  @Schema(description = "盒子的 UUID")
  private String boxUUID;

  @NotBlank
  @Schema(description = "盒子的注册码")
  private String boxRegKey;

  @NotBlank
  @Schema(description = "用户的 ID")
  private String userId;

  @Schema(description = "用户被指定的子域名字段")
  private String subdomain;

  @NotBlank
  @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
  @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
  private String userType;

  @NotBlank
  @Schema(description = "客户端的 UUID")
  private String clientUUID;
}
