package xyz.eulix.platform.services.support.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum SortDirEnum {
    ASC("asc", "正序"),
    DESC("desc", "倒序"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static SortDirEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> {
            if (appType.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
