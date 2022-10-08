package xyz.eulix.platform.services.token.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class TokenInfo {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @NotEmpty
    @Schema(description = "平台id：空间平台（serviceId=10001）、产品服务平台（serviceId=10002）")
    private List<@ValueOfEnum(enumClass = ServiceEnum.class, valueMethod = "getServiceId") String> serviceIds;

    @Schema(description = "签名，使用公钥验证盒子身份时必传")
    private String sign;
}
