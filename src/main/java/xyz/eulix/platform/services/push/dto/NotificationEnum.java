package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.eulix.platform.services.support.boundary.push.AfterOpenAction;

@AllArgsConstructor
public enum NotificationEnum {
    BOX_UPGRADE("系统升级", "发现新的傲空间系统版本，点击查看", "系统升级推送", "box_upgrade", AfterOpenAction.GO_ACTIVITY, "xyz.eulix.space.push.EulixMfrNotifyActivity"),
    APP_UPGRADE("应用升级", "发现新的傲空间应用版本，点击查看", "应用升级推送", "app_upgrade", AfterOpenAction.GO_ACTIVITY, "xyz.eulix.space.push.EulixMfrNotifyActivity"),
    ;

    @Getter
    private final String title;
    @Getter
    private final String text;
    @Getter
    private final String desc;
    @Getter
    private final String type;
    @Getter
    private final AfterOpenAction afterOpenAction;
    @Getter
    private final String activity;

    public static NotificationEnum of(String type) {
        for (var notification : NotificationEnum.values()) {
            if (notification.type.equals(type)) {
                return notification;
            }
        }
        throw new IllegalArgumentException("Type is invalid");
    }
}
