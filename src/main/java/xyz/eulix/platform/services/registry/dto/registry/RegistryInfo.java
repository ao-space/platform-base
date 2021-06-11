package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class RegistryInfo {
  @NotNull
  private String boxUUID;

  @NotNull
  private String clientUUID;

  @NotBlank
  private String subdomain;
}
