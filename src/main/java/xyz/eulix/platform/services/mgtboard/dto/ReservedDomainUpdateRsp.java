package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class ReservedDomainUpdateRsp {
    @Schema(description = "更新的条目数量")
    private final Long updatedCount;
}
