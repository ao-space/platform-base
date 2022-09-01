package xyz.eulix.platform.services.applet.dto.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Data
public class AppletReq {
	@Schema(description = "当前盒子版本")
	private String curBoxVersion;
}
