package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class RegistryResult {
  private final String clientRegKey;
  private final String boxRegKey;
  private final String userDomain;
  private final TunnelServer tunnelServer;
}
