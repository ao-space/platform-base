package xyz.eulix.platform.services.applet.dto;

import lombok.Data;

import java.util.List;

@Data(staticConstructor = "of")
public class CheckAppletResult {
	private final  Boolean result;
	// 如果检查通过，返回小应用申请需要的服务能力分组（categories，例如：userinfo-readonly,addressbook）
	private final List<String> categories;
	// 客户端应用基本信息
	private final AppletInfo appletInfo;

	@Data(staticConstructor = "of")
	public static class AppletInfo {
		private final String name;
		private final String description;
		private final String iconUrl;
	}
}
