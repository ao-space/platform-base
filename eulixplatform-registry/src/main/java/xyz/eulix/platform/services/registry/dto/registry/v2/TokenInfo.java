package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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

    @NotBlank
    @Schema(description = "平台id：空间平台（serviceId=10001）、产品服务平台（serviceId=10002）")
    private List<String> serviceIds;

    @NotBlank
    @Schema(description = "签名")
    private String sign;
}
