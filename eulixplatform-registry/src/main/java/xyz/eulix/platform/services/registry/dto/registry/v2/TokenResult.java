package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class TokenResult {
    @Schema(description = "平台id")
    private String serviceId;

    @Schema(description = "盒子在当前平台的注册码")
    private String boxRegKey;
}
