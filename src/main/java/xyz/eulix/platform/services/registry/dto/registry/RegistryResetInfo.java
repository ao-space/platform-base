package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of activation cancel information.
 */
@Data
public class RegistryResetInfo {
  @NotBlank
  private String boxUUID;

  @NotBlank
  private String boxRegKey;
}
