package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Data(staticConstructor = "of")
public class ReservedDomainDeleteRsp {
    @Schema(description = "删除的条目数量")
    private final Long deletedCount;
}
