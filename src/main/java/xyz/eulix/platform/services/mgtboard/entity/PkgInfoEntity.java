package xyz.eulix.platform.services.mgtboard.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.mgtboard.dto.PkgTypeEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "pkg_info")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class PkgInfoEntity extends BaseEntity {
  // pkg名称
  @NotNull
  @Column(name = "pkg_name")
  private String pkgName;

  // pkg类型 ios、android
  @NotNull
  @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
  @Column(name = "pkg_type")
  private String pkgType;

  // 版本号 长度0-20个字符
  @NotNull
  @Column(name = "pkg_version")
  private String pkgVersion;

  // 版本文件大小(字节)，最大10GB
  @Column(name = "pkg_size")
  private Long pkgSize;

  // 更新文案/版本特性 长度0-10000个字符
  @Column(name = "update_desc")
  private String updateDesc;

  // 是否强制更新 1-强制更新;0-可选更新
  @Column(name = "force_update")
  private Boolean isForceUpdate;

  // 下载url
  @Column(name = "download_url")
  private String downloadUrl;

  // md5
  @Column(name = "md5")
  private String md5;

  // 最小兼容版本
  @Column(name = "min_compatible_version")
  private String minCompatibleVersion;

  // 扩展信息，json结构
  @Column(name = "extra")
  private String extra;
}
