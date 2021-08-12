package xyz.eulix.platform.services.auth.dto;

import lombok.Data;

@Data(staticConstructor = "of")
public class GenPkeyRsp {
    // 二维码pkey值
    private final String pkey;

    // 过期时间
    private final String expiresAt;
}
