package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.*;
import java.util.List;

@Data(staticConstructor = "of")
public class ProposalReq {
    // 反馈内容
    @NotBlank
    @Size(max = 500)
    @Schema(required = true, maxLength = 500 ,description = "反馈内容")
    private String content;

    // 邮箱
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")
    @Schema(pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", description = "邮箱")
    private String email;

    // 手机号码
    @Pattern(regexp = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$")
    @Schema(pattern = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$", description = "手机号码")
    private String phoneNumer;

    // 图片地址，最多4张
    @Size(max = 4)
    @Schema(maxLength = 4 ,description = "图片地址")
    private List<String> imageUrls;
}
