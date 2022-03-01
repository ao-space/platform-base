package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum SortKeyEnum {
    PKG_TYPE("pkg_type", "软件包类型"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static SortKeyEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> {
            if (appType.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
