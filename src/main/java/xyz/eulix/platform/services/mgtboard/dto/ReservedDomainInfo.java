package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class ReservedDomainInfo {
    @Schema(description = "条目id")
    private final Long regexId;

    @Schema(description = "正则表达式")
    private final String regex;

    @Schema(description = "规则描述")
    private final String desc;

    @Schema(description = "更新时间")
    private final OffsetDateTime updatedAt;
}
