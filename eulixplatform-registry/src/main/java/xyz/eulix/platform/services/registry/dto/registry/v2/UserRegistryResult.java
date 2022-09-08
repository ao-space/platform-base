package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class UserRegistryResult {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "用户的 ID")
    private final String userId;

    @Schema(description = "为用户分配的用户域名，该域名可以用于后续的业务访问")
    private final String userDomain;

    @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
    private final String userType;

    @Schema(description = "客户端的 UUID")
    private final String clientUUID;
}
