package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor(staticName = "of")
public class FeedbackPayloadReq {
    @Schema(description = "问卷ID")
    private Integer survey_id;

    @Schema(description = "答案ID")
    private Integer answer_id;

    @Schema(description = "自定义信息 ")
    private String openid;

    @Schema(description = "用户开始回答的时间")
    private String started_at;

    @Schema(description = "用户提交答案的时间")
    private String ended_at;

    @Schema(description = "用户的回答详情")
    private String answer;
}