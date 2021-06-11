package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of registry reset result.
 */
@Data(staticConstructor = "of")
public class RegistryResetResult {
  private final String boxUUID;
}
