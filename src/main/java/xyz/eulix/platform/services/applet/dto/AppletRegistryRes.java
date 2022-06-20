package xyz.eulix.platform.services.applet.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class AppletRegistryRes {
	@Schema(description = "appletid")
	private final String appletId;

	@Schema(description = "applet_secret")
	private final String appletSecret;
}
