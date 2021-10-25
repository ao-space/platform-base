package xyz.eulix.platform.services.mgtboard.entity;

import lombok.*;
import xyz.eulix.platform.services.mgtboard.dto.PkgTypeEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.*;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "app_info")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class AppInfoEntity extends BaseEntity {
    // App名称
    @NotNull
    @Column(name = "app_name")
    private String appName;

    // App类型 ios、android
    @NotNull
    @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
    @Column(name = "app_type")
    private String appType;

    // 版本号 长度0-20个字符
    @NotNull
    @Column(name = "app_version")
    private String appVersion;

    // 版本文件大小(字节)，最大10GB
    @Column(name = "app_size")
    private Long appSize;

    // 下载url
    @Column(name = "download_url")
    private String downloadUrl;

    // 更新文案/版本特性 长度0-10000个字符
    @Column(name = "update_desc")
    private String updateDesc;

    // md5
    @Column(name = "md5")
    private String md5;

    // 是否强制更新 1-强制更新;0-可选更新
    @Column(name = "force_update")
    private Boolean isForceUpdate;

    // 扩展信息，json结构
    @Column(name = "extra")
    private String extra;
}
