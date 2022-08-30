package xyz.eulix.platform.services.push.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.push.dto.DeviceTypeEnum;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "of")
public class DeviceTokenReq {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @NotBlank
    @Schema(description = "用户的 ID")
    private String userId;

    @NotBlank
    @Schema(description = "客户端的 UUID")
    private String clientUUID;

    @NotBlank
    @Schema(description = "友盟 device token")
    private String deviceToken;

    @NotNull
    @ValueOfEnum(enumClass = DeviceTypeEnum.class, valueMethod = "getName")
    @Schema(description = "设备类型,枚举值：android/ios/harmony")
    private String deviceType;

    @Schema(description = "扩展信息,json格式")
    private String extra;
}