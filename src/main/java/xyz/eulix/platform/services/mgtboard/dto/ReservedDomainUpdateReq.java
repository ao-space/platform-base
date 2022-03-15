package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class ReservedDomainUpdateReq {
    @NotBlank
    @Schema(required = true, description = "正则表达式")
    private String regex;

    @NotBlank
    @Schema(required = true, description = "规则描述")
    private String desc;
}
