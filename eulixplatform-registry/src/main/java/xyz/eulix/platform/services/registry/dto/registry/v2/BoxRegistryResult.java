package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.registry.dto.registry.NetworkClient;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class BoxRegistryResult {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "为盒子分配的 network client 信息")
    private final NetworkClient networkClient;
}
