package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class SubdomainGetResult {
    @Schema(description = "盒子的UUID")
    private final String boxUUID;

    @Schema(description = "用户ID")
    private final String userId;

    @Schema(description = "全subdomain")
    private final String subdomain;

    @Schema(description = "用户域名")
    private final String userDomain;

    @Schema(description = "状态，取值：0-临时,1-已使用,2-历史使用")
    private final Integer state;

    @Schema(description = "过期时间")
    private final OffsetDateTime expiresAt;
}
