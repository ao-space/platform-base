package xyz.eulix.platform.services.auth.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data(staticConstructor = "of")
public class PollPkeyReq {
    // 二维码pkey值
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9-]{36}")
    @Schema(pattern = "[a-zA-Z0-9-]{36}", description = "二维码pkey值")
    private String pkey;

}
