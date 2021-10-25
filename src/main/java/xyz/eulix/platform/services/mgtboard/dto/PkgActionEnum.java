package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum PkgActionEnum {
    APP_SAVE("app_name", "保存app软件包"),
    BOX_SAVE("box_save", "保存box软件包"),
    APP_UPDATE("app_update", "更新app软件包"),
    BOX_UPDATE("box_update", "更新box软件包"),
    APP_CHECK("app_check", "检查app软件包更新"),
    BOX_CHECK("box_check", "检查box软件包更新"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static PkgActionEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> {
            if (appType.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
