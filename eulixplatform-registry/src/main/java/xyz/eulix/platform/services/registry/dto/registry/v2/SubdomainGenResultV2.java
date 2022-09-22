package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class SubdomainGenResultV2 {
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @Schema(description = "全局唯一的 subdomain")
    private String subdomain;

    @Schema(description = "subdomain过期时间")
    private OffsetDateTime expiresAt;
}
