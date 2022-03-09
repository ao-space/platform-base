package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class CatalogueRes {
    @Schema(description = "id")
    private final Long id;

    @Schema(description = "目录名字")
    private final String cataName;

    @Schema(description = "父目录id")
    private final Long parentId;

    @Schema(description = "创建时间")
    private final OffsetDateTime createdAt;

    @Schema(description = "修改时间")
    private final OffsetDateTime updatedAt;
}
