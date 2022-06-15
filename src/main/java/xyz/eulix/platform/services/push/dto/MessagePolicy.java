package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MessagePolicy {
    @Schema(description = "可选，定时发送时，若不填写表示立即发送。格式: YYYY-MM-DD hh:mm:ss")
    private String startTime;

    @Schema(description = "可选，消息过期时间，其值不可小于发送时间或者startTime。如果不填写此参数，默认为3天后过期。格式: YYYY-MM-DD hh:mm:ss")
    private String expireTime;
}
