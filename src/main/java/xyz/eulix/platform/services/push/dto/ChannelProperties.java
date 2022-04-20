package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor(staticName = "of")
public class ChannelProperties {
    @NotEmpty
    @Schema(description = "必选，厂商通道相关的特殊配置。系统弹窗，只有display_type=notification时有效，表示华为、小米、oppo、vivo、魅族的设备离线时走系统通道下发时打开指定页面acitivity的完整包路径")
    private String channelActivity;
}
