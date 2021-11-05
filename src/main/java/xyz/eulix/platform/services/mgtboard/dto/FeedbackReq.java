package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "of")
public class FeedbackReq {
    @Schema(description = "uuid")
    private String id;

    @Schema(description = "对象")
    private String object;

    @Schema(description = "行动")
    private String action;

    @Schema(description = "推送时间")
    private String created_at;

    @NotNull
    @Schema(required = true, description = "具体内容")
    private FeedbackPayloadReq payload;
}