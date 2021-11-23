package xyz.eulix.platform.services.mgtboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class FeedbackRes {
    @Schema(description = "uuid")
    private final String id;

    @Schema(description = "对象")
    private final String object;

    @Schema(description = "行动")
    private final String action;

    @Schema(description = "推送时间")
    @JsonProperty("created_at")
    private final String createdAt;
}
