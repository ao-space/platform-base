package xyz.eulix.platform.services.push.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class DeviceTokenRes {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "用户的 ID")
    private final String userId;

    @Schema(description = "客户端的 UUID")
    private final String clientUUID;

    @Schema(description = "友盟 device token")
    private final String deviceToken;

    @Schema(description = "设备类型,枚举值：android/ios/harmony")
    private final String deviceType;

    @Schema(description = "扩展信息,json格式")
    private final String extra;

    @Schema(description = "创建时间")
    private final OffsetDateTime createAt;

    @Schema(description = "更新时间")
    private final OffsetDateTime updateAt;
}
