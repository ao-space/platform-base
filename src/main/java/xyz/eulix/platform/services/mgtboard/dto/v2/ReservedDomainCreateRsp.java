package xyz.eulix.platform.services.mgtboard.dto.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data(staticConstructor = "of")
public class ReservedDomainCreateRsp {
    @Schema(description = "条目id")
    private Long domainId;

    @Schema(description = "正则表达式")
    private String regex;

    @Schema(description = "规则描述")
    private String desc;
}
