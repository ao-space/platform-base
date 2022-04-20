package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "of")
public class MessagePolicy {
    @Schema(description = "可选，定时发送时，若不填写表示立即发送")
    private String startTime;

    @Schema(description = "可选，消息过期时间，其值不可小于发送时间或者startTime。如果不填写此参数，默认为3天后过期。")
    private String expireTime;

    @Schema(description = "可选，消息发送接口对任务类消息的幂等性保证。同一个appkey下面的多个消息会根据out_biz_no去重，不同发送任务的out_biz_no需要保证不同，否则会出现后发消息被去重过滤的情况。")
    private String outBizNo;
}
