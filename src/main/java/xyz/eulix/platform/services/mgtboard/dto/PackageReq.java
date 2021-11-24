package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.constraints.*;


@Data
@AllArgsConstructor(staticName = "of")
public class PackageReq {
    // package标识符
    @NotBlank
    @Schema(required = true)
    private String pkgName;

    // package类型 ios、android
    @NotNull
    @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
    @Schema(enumeration = {"android", "ios", "box"})
    private String pkgType;

    // 版本号 长度0-50个字符
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}")
    @Schema(pattern = "[a-zA-Z0-9.-]{0,50}", description = "版本号")
    private String pkgVersion;

    // 版本文件大小(字节)，最大10GB
    @Min(0)
    @Max(10737418240L)
    @Schema(minimum = "0", maximum = "10737418240", description = "文件大小，单位byte")
    private Long pkgSize;

    // 下载url
    @NotBlank
    @Schema(required = true)
    private String downloadUrl;

    // 更新文案/版本特性 长度0-10000个字符
    @Size(max = 10000)
    @Schema(description = "版本特性")
    private String updateDesc;

    // md5
    @Pattern(regexp = "[0-9a-fA-F]{32}")
    @Schema(pattern = "[0-9a-fA-F]{32}")
    private String md5;

    // 是否强制更新 1-强制更新;0-可选更新
    @Schema(description = "是否强制更新")
    private Boolean isForceUpdate;

    // 最小兼容版本（盒子，兼容的最小Android版本）
    @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}")
    @Schema(pattern = "[a-zA-Z0-9.-]{0,50}", description = "最小兼容版本（更新盒子，兼容的最小App版本；更新App，兼容的最小盒子版本；")
    private String minAndroidVersion;

    // 最小兼容版本（盒子，兼容的最小IOS版本）
    @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}")
    @Schema(pattern = "[a-zA-Z0-9.-]{0,50}", description = "最小兼容版本（更新盒子，兼容的最小App版本；更新App，兼容的最小盒子版本；")
    private String minIOSVersion;


    // 最小兼容版本（App，兼容的最小盒子版本；）
    @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}")
    @Schema(pattern = "[a-zA-Z0-9.-]{0,50}", description = "最小兼容版本（更新盒子，兼容的最小App版本；更新App，兼容的最小盒子版本；")
    private String minBoxVersion;
}
