package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor(staticName = "of")
public class QuestionnaireUpdateReq {
    @NotBlank
    @Size(max = 1024)
    @Schema(required = true, maxLength = 1024, description = "标题")
    private String title;

    @Schema(description = "开始时间")
    private OffsetDateTime startAt;

    @Schema(description = "结束时间")
    private OffsetDateTime endAt;
}