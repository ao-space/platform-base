package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 客户端割接结果
 */
@Data(staticConstructor = "of")
public class ClientMigrationResult {
    @Schema(description = "客户端的 UUID")
    private final String clientUUID;

    @Schema(description = "用户的注册码，用于后续平台对于用户访问合法性的验证")
    private final String clientRegKey;

    @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth")
    private String clientType;
}
