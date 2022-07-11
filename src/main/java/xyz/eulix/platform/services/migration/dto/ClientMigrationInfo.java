package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;

/**
 * 客户端割接信息
 */
@Data
public class ClientMigrationInfo {
    @NotBlank
    @Schema(description = "客户端的 UUID")
    private String clientUUID;

    @NotBlank
    @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth")
    @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
    private String clientType;
}
