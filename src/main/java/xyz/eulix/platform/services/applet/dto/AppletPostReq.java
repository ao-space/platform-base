package xyz.eulix.platform.services.applet.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Data
public class AppletPostReq {
	@Schema(description = "小程序名字")
	private String appletName;

	@Schema(description = "小程序英文名")
	private String appletNameEn;

	@Schema(description = "小程序发布状态", enumeration = {"0", "1"})
	private Integer state;

	@Schema(description = "小程序version")
	private String appletVersion;

	@Schema(description = "applet_size")
	private Long appletSize;

	@Schema(description = "update_desc")
	private String updateDesc;

	@Schema(description = "icon_url")
	private String iconUrl;

	@Schema(description = "down_url")
	private String downUrl;

	@Schema(description = "categories,小程序所需求的权限，以逗号分割")
	private String categories;

	@NotNull
	@Schema(description = "md5")
	private String md5;

	@Schema(description = "是否强制更新")
	private Boolean isForceUpdate;

	@Schema(description = "兼容盒子最小版本")
	private String minCompatibleBoxVersion;
}
