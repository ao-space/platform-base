package xyz.eulix.platform.services.mgtboard.dto.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class ReservedDomainUpdateRsp {
    @Schema(description = "条目id")
    private Long domainId;

    @Schema(description = "正则表达式")
    private String regex;

    @Schema(description = "规则描述")
    private String desc;
}
