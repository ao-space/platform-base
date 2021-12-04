package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of activation cancel information.
 */
@Data
public class UserRegistryResetInfo {
  @NotBlank
  @Schema(description = "盒子的 UUID")
  private String boxUUID;

  @NotBlank
  @Schema(description = "用户域名")
  private String userDomain;

  @NotBlank
  @Schema(description = "用户的注册码")
  private String userRegKey;
}
