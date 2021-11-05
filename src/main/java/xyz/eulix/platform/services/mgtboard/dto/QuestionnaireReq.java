package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor(staticName = "of")
public class QuestionnaireReq {
    @NotBlank
    @Size(max = 1024)
    @Schema(required = true, maxLength = 1024, description = "标题")
    private String title;

    @NotBlank
    @Size(max = 1024)
    @Schema(required = true, maxLength = 1024, description = "问卷链接")
    private String content;

    @Schema(description = "开始时间")
    private OffsetDateTime startAt;

    @Schema(description = "结束时间")
    private OffsetDateTime endAt;

    @NotNull
    @Schema(required = true, description = "第三方问卷id")
    private Integer payloadSurveyId;
}