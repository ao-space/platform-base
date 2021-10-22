package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class BoxPkgRes {
    // box软件包名称
    private final String boxPkgName;

    // 版本号 长度0-20个字符
    private final String boxPkgVersion;

    // 版本文件大小(字节)，最大10GB
    private final Long boxPkgSize;

    // 下载url
    private final String downloadUrl;

    @Schema(description = "版本特性")
    private final String updateDesc;

    // md5
    private final String md5;

    @Schema(description = "是否强制更新")
    private final Boolean isForceUpdate;

    @Schema(description = "兼容的最小App版本")
    private final String minAppVersion;
}
