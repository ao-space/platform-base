package xyz.eulix.platform.services.token.dto;

import java.time.OffsetDateTime;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class TokenResult {
    @Schema(description = "平台id")
    private final String serviceId;

    @Schema(description = "盒子在当前平台的注册码")
    private final String boxRegKey;

    @Schema(description = "注册码 token 有效时间, OffsetDateTime 类型")
    private final OffsetDateTime expiresAt;
}
