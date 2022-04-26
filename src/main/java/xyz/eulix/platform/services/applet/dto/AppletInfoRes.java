package xyz.eulix.platform.services.applet.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class AppletInfoRes {
	@Schema(description = "小程序名字")
	private final String name;

	@Schema(description = "小程序英文名字")
	private final String nameEn;

	@Schema(description = "小程序发布状态：0-支持安装;1-敬请期待")
	private final Integer state;

	@Schema(description = "appletid")
	private final String appletId;

	@Schema(description = "md5")
	private final String md5;

	@Schema(description = "小程序版本")
	private final String appletVersion;

	@Schema(description = "iconurl")
	private final String iconUrl;

	@Schema(description = "上一次更新时间")
	private final OffsetDateTime updateAt;
}
