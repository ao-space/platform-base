package xyz.eulix.platform.services.mgtboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor(staticName = "of")
public class FeedbackPayloadReq {
    @Schema(description = "问卷ID")
    @JsonProperty("survey_id")
    private Long surveyId;

    @Schema(description = "答案ID")
    @JsonProperty("answer_id")
    private Long answerId;

    @Schema(description = "自定义信息 ")
    @JsonProperty("openid")
    private String openId;

    @Schema(description = "用户开始回答的时间")
    @JsonProperty("started_at")
    private String startedAt;

    @Schema(description = "用户提交答案的时间")
    @JsonProperty("ended_at")
    private String endedAt;

    @Schema(description = "用户的回答详情")
    private Object answer;
}