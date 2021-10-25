package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum PkgTypeEnum {
    ANDROID("android", "安卓"),
    IOS("ios", "苹果"),
    BOX("box", "傲来盒子"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static PkgTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> {
            if (appType.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
