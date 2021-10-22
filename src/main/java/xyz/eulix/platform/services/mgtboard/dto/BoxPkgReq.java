package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.*;


@Data(staticConstructor = "of")
public class BoxPkgReq {
    // Box标识符
    @NotBlank
    @Schema(required = true)
    private String boxPkgName;

    // 版本号 长度0-50个字符
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}")
    @Schema(pattern = "[a-zA-Z0-9.-]{0,50}", description = "版本号")
    private String boxPkgVersion;

    // 版本文件大小(字节)，最大10GB
    @Min(0)
    @Max(10737418240L)
    @Schema(minimum = "0", maximum = "10737418240", description = "文件大小，单位byte")
    private Long boxPkgSize;

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
    private Boolean isForceUpdate = false;

    // 兼容的最小App版本
    @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}")
    @Schema(pattern = "[a-zA-Z0-9.-]{0,50}", description = "兼容的最小App版本")
    private String minAppVersion;
}
