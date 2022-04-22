package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@AllArgsConstructor(staticName = "of")
public class MessagePayload {
    @NotNull
    @ValueOfEnum(enumClass = DisplayTypeEnum.class, valueMethod = "getName")
    @Schema(description = "消息类型，枚举：notification-通知/message-消息")
    private String displayType;

    @Valid
    @NotNull
    @Schema(description = "消息体")
    private MessagePayloadBody body;

    @Schema(description = "可选，Map格式，用户自定义key-value")
    private Map<String, String> extra;
}
