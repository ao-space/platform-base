package xyz.eulix.platform.services.auth.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class TransBoxInfoReq {
    // 二维码pkey值
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9-]{36}")
    @Schema(pattern = "[a-zA-Z0-9-]{36}", description = "二维码pkey值")
    private String pkey;

    // 登录box端的key
    @NotBlank
    @Size(max = 128)
    @Schema(maxLength = 128 ,description = "盒子侧bkey值")
    private String bkey;

    @NotBlank
    @Size(max = 128)
    @Schema(maxLength = 128 ,description = "用户域名")
    private String userDomain;

    // 盒子公钥
    @NotBlank
    @Size(max = 1024)
    @Schema(maxLength = 1024 ,description = "盒子公钥")
    private String boxPubKey;
}