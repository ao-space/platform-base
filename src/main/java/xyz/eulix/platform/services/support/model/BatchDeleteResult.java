package xyz.eulix.platform.services.support.model;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class BatchDeleteResult {
    @Schema(description = "删除的条目数量")
    private Long deletedCount;
}
