package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "of")
public class MessagePayloadBody {
    @Schema(description = "通知标题，当displayType=notification时必填")
    private String text;

    @Schema(description = "通知文字描述，当displayType=notification时必填")
    private String title;

    @Schema(description = "点击通知的后续行为(默认为打开app)，当displayType=notification时必填")
    private String afterOpen;

    @Schema(description = "通知栏点击后跳转的URL，当after_open=go_url时必填")
    private String url;

    @Schema(description = "通知栏点击后打开的Activity，当afterOpen=go_activity时必填")
    private String activity;

    @Schema(description = "用户自定义内容，可以为字符串或者JSON格式。当display_type=message时,或者当display_type=notification且after_open=go_custom时，必填")
    private String custom;
}
