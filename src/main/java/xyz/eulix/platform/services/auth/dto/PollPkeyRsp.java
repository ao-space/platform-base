package xyz.eulix.platform.services.auth.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class PollPkeyRsp {
    // 二维码pkey值
    @Schema(description = "二维码pkey值")
    private final String pkey;

    // 登录box端的key
    @Schema(description = "盒子侧bkey值")
    private final String bkey;

    // 盒子域名
    @Schema(description = "盒子域名")
    private final String boxDomain;

    // 盒子公钥
    @Schema(description = "盒子公钥")
    private final String boxPubKey;
}
