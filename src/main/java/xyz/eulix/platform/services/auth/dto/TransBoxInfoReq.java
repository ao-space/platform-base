package xyz.eulix.platform.services.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TransBoxInfoReq {
    // 二维码pkey值
    @NotBlank
    private String pkey;

    // 登录box端的key
    @NotBlank
    private String bkey;

    // 盒子域名
    @NotBlank
    private String boxDomain;

    // 盒子公钥
    @NotBlank
    private String boxPubKey;
}
