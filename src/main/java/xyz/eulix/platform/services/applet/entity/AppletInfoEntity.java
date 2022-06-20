package xyz.eulix.platform.services.applet.entity;

import lombok.Data;
import xyz.eulix.platform.services.applet.service.AppletStatus;

import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Data
@Entity
@Table(name = "applet_info")
public class AppletInfoEntity extends BaseEntity{
	@NotBlank
	@Column(name = "applet_name")
	private String name;

	@Column(name = "applet_en_name")
	private String nameEn;

	@Column(name = "state")
	@ValueOfEnum(enumClass = AppletStatus.class, valueMethod = "getState")
	private Integer state;

	@Column(name = "applet_id")
	private String appletId;

	@Column(name="applet_version")
	private String appletVersion;

	@Column(name="applet_secret")
	private String appletSecret;

	// 版本文件大小(字节)，最大10GB
	@Column(name = "applet_size")
	private Long appletSize;

	// 更新文案/版本特性 长度0-10000个字符
	@Column(name = "update_desc")
	private String updateDesc;

	// 是否强制更新 1-强制更新;0-可选更新
	@Column(name = "force_update")
	private Boolean isForceUpdate;

	//小图标下载url
	@Column(name = "icon_url")
	private String iconUrl;

	//程序下载url
	@Column(name = "down_url")
	private String downUrl;

	//小程序所求权限
	@Column(name = "categories")
	private String categories;

	// md5
	@Column(name = "md5")
	private String md5;

	// 最小 box 兼容版本
	@Column(name = "min_compatible_box_version")
	private String minCompatibleBoxVersion;
}
