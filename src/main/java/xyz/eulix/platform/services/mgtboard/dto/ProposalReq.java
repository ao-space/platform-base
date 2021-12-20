package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.*;
import java.util.List;

@Data(staticConstructor = "of")
public class ProposalReq {
    @Size(max = 128)
    @Schema(maxLength = 128 ,description = "用户域名")
    private String userDomain;

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
    @Pattern(regexp = "(((\\+86)|(86))?1[3|5|7|8|][0-9]{9})|(\\d{3}-\\d{8})|(\\d{4}-\\d{7})")
    @Schema(pattern = "(((\\+86)|(86))?1[3|5|7|8|][0-9]{9})|(\\d{3}-\\d{8})|(\\d{4}-\\d{7})", description = "电话号码")
    private String phoneNumber;

    // 图片地址，最多4张
    @Size(max = 4)
    @Schema(maxLength = 4 ,description = "图片地址")
    private List<String> imageUrls;
}
