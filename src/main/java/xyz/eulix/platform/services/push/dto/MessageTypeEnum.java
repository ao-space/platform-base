package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum MessageTypeEnum {
    CLIENTCAST("clientcast", "推送目标设备"),
    BROADCAST("broadcast", "广播"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static MessageTypeEnum fromValue(String name) {
        return Arrays.stream(values()).filter(value -> {
            if (value.getName().equals(name)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
