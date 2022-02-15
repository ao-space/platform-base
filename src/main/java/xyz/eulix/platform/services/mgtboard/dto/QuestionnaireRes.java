package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data(staticConstructor = "of")
public class QuestionnaireRes {
    @Schema(description = "问卷id")
    private final Long questionnaireId;

    @Schema(description = "标题")
    private final String title;

    @Schema(description = "问卷链接")
    private final String content;

    @Schema(description = "开始时间")
    private final OffsetDateTime startAt;

    @Schema(description = "结束时间")
    private final OffsetDateTime endAt;

    @Schema(description = "第三方问卷id")
    private final Long payloadSurveyId;

    @Schema(description = "问卷状态，携带用户信息查询时返回", enumeration = {"completed", "not_start", "in_process", "has_end"})
    private String state;

    @Schema(description = "已反馈数量")
    private Long feedbackStatistic;
}
