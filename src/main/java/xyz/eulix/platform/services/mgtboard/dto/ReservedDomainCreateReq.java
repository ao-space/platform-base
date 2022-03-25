package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data(staticConstructor = "of")
public class ReservedDomainCreateReq {
    @NotBlank
    @Size(max = 100,min = 1)
    @Schema(required = true, description = "正则表达式", maxLength = 100, minLength = 1)
    private String regex;

    @NotBlank
    @Size(max = 60)
    @Schema(required = true, description = "规则描述", maxLength = 60)
    private String desc;
}
