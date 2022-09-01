package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class ClientRegistryResult {

    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @Schema(description = "用户的 ID")
    private String userId;

    @Schema(description = "客户端的 UUID")
    private String clientUUID;

    @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth")
    private String clientType;
}
