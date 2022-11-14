package xyz.eulix.platform.services.token.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class CheckTokenInfo {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @NotBlank
    @ValueOfEnum(enumClass = ServiceEnum.class, valueMethod = "getServiceId")
    @Schema(description = "平台id：空间平台（serviceId=10001）、产品服务平台（serviceId=10002）")
    private String serviceId;

    @NotBlank
    @Schema(description = "盒子在当前平台的注册码")
    private String boxRegKey;
}
