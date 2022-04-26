package xyz.eulix.platform.services.applet.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Data
public class AppletReq {
	@NotNull
	@Schema(required = true, description = "appletId")
	private String appletId;

	@Schema(description = "当前盒子版本")
	private String curBoxVersion;
}
