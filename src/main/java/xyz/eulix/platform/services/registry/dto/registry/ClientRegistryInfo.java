package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
  @Schema(description = "客户端的 UUID")
  private String clientUUID;

  @NotBlank
  @Schema(description = "盒子的注册码")
  private String boxRegKey;

  @Schema(description = "客户端被指定的子域名字段")
  private String subdomain;
}
