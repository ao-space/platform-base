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

	@NotNull
	@Schema(required = true, description = "appletId")
	private String appletId;

	@NotNull
	@Schema(description = "小程序version")
	private String appletVersion;

	@NotNull
	@Schema(description = "applet_size")
	private Long appletSize;

	@Schema(description = "update_desc")
	private String updateDesc;

	@Schema(description = "icon_url")
	private String iconUrl;

	@Schema(description = "down_url")
	private String downUrl;

	@NotNull
	@Schema(description = "md5")
	private String md5;

	@Schema(description = "兼容盒子最小版本")
	private String minCompatibleBoxVersion;
}
