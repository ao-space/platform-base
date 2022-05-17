package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum DisplayTypeEnum {
    NOTIFICATION("notification", "通知"),
    MESSAGE("message", "消息"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static DisplayTypeEnum fromValue(String name) {
        return Arrays.stream(values()).filter(value -> {
            if (value.getName().equals(name)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
