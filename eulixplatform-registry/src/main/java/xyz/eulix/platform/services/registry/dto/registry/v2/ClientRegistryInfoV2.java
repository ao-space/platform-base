package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class ClientRegistryInfoV2 {
    @NotBlank
    @Schema(description = "客户端的 UUID")
    private String clientUUID;

    @NotBlank
    @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth")
    @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
    private String clientType;
}
