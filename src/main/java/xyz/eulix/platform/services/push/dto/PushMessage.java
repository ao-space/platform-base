package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
public class PushMessage {
    @NotNull
    @ValueOfEnum(enumClass = MessageTypeEnum.class, valueMethod = "getName")
    @Schema(description = "消息发送类型,枚举值：clientcast-推送目标设备/broadcast-广播")
    private String type;

    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @NotBlank
    @Schema(description = "盒子的注册码")
    private String boxRegKey;

    @Schema(description = "用户的 ID列表,当type=user_cast时,必填")
    private List<String> userIds;

    @Valid
    @Schema(description = "客户端的 UUID列表,当type=client_cast时,必填")
    private List<UserIdAndClientUUID> clientUUIDs;

    @Schema(description = "发送消息描述,可选")
    private String description;

    @Valid
    @NotNull
    @Schema(description = "消息")
    private MessagePayload payload;

    @Schema(description = "发送策略")
    private MessagePolicy policy;

    @Valid
    @NotNull
    @Schema(description = "厂商通道相关的特殊配置")
    private ChannelProperties channelProperties;

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class UserIdAndClientUUID {
        @NotBlank
        @Schema(description = "用户的 ID")
        private String userId;

        @NotBlank
        @Schema(description = "客户端的 UUID")
        private String clientUUID;
    }
}