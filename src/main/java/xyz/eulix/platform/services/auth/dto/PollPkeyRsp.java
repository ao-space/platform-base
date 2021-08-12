package xyz.eulix.platform.services.auth.dto;

import lombok.Data;

@Data(staticConstructor = "of")
public class PollPkeyRsp {
    // 二维码pkey值
    private final String pkey;

    // 登录box端的key
    private final String bkey;

    // 盒子域名
    private final String boxDomain;

    // 盒子公钥
    private final String boxPubKey;
}
