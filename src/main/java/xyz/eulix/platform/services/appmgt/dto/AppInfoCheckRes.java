package xyz.eulix.platform.services.appmgt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class AppInfoCheckRes {
    // 是否更新
    @Schema(description = "是否更新")
    private Boolean isUpdate = false;

    // 是否强制更新
    @Schema(description = "是否强制更新")
    private Boolean isForceUpdate = false;

    // App名称
    private final String bundleId;

    // App类型
    private final String platform;

    // 最新版本号
    @Schema(description = "最新版本号")
    private final String newestVersion;

    // 版本文件大小
    @Schema(description = "文件大小，单位byte")
    private final Long appSize;

    // 下载url
    private final String downloadUrl;

    // 更新文案/版本特性
    @Schema(description = "版本特性")
    private final String updateDesc;

    // md5
    private final String md5;
}
