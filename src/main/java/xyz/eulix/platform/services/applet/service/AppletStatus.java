package xyz.eulix.platform.services.applet.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum AppletStatus {
	NORMAL(0, "支持安装"),
	PENDING(1, "敬请期待");

    @Getter @Setter
	private Integer state;

    @Getter @Setter
	private String desc;
}
