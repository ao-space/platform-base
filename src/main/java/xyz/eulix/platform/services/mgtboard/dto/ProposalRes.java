package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data(staticConstructor = "of")
public class ProposalRes {
    // id
    @Schema(description = "id")
    private final Long proposalId;

    @Schema(description = "用户域名")
    private final String userDomain;

    // 反馈内容
    @Schema(description = "反馈内容")
    private final String content;

    // 邮箱
    @Schema(description = "邮箱")
    private final String email;

    // 手机号码
    @Schema(description = "手机号码")
    private final String phoneNumber;

    // 图片地址
    @Schema(description = "图片地址")
    private final List<String> imageUrls;
}
