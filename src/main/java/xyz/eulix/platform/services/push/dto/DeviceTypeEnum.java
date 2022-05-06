package xyz.eulix.platform.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum DeviceTypeEnum {
    ANDROID("android", "安卓"),
    IOS("ios", "苹果"),
    HARMONY("harmony", "鸿蒙"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static DeviceTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(state -> {
            if (state.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
