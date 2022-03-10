package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class PackageRes {
    // 软件包名称
    @Schema(description = "id")
    private final Long id;

    // 软件包名称
    @Schema(description = "软件包标识符")
    private final String pkgName;

    // 软件包类型 ios、android、box
    @Schema(description = "软件包类型", enumeration = {"android", "ios", "box"})
    private final String pkgType;

    // 版本号 长度0-20个字符
    @Schema(description = "软件包版本")
    private final String pkgVersion;

    // 版本文件大小(字节)，最大10GB
    private final Long pkgSize;

    // 下载url
    private final String downloadUrl;

    // 更新文案/版本特性 长度0-10000个字符
    @Schema(description = "版本特性")
    private final String updateDesc;

    // md5
    private final String md5;

    // 是否强制更新 1-强制更新;0-可选更新
    @Schema(description = "是否强制更新")
    private final Boolean isForceUpdate;

    @Schema(description = "兼容的最小App版本,用于box版本")
    private final String minAndroidVersion;

    @Schema(description = "兼容的最小App版本,用于box版本")
    private final String minIOSVersion;

    // 所需的最小盒子版本
    @Schema(description = "所需的最小盒子版本,用于app版本")
    private final String minBoxVersion;

    @Schema(description = "创建时间")
    private final OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private final OffsetDateTime updatedAt;
}
