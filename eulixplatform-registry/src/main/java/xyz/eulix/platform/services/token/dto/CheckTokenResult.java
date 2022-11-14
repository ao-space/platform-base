package xyz.eulix.platform.services.token.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class CheckTokenResult {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "平台id")
    private final String serviceId;

    @Schema(description = "平台名称")
    private final String serviceName;

    @Schema(description = "注册码 token 有效时间, OffsetDateTime 类型")
    private final OffsetDateTime expiresAt;
}
