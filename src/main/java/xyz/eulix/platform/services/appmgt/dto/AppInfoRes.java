package xyz.eulix.platform.services.appmgt.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data(staticConstructor = "of")
public class AppInfoRes {
    // App名称
    private final String appName;

    // App类型 ios、android
    private final AppTypeEnum appType;

    // 版本号 长度0-20个字符
    private final String appVersion;

    // 版本文件大小(字节)，最大10GB
    private final Long appSize;

    // 下载url
    private final String downloadUrl;

    // 更新文案/版本特性 长度0-10000个字符
    private final String updateDesc;

    // md5
    private final String md5;

    // 是否强制更新 1-强制更新;0-可选更新
    private final Boolean forceUpdate;
}
