package xyz.eulix.platform.services.network.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum NetworkServerStateEnum {
    OFFLINE(0, "未启用"),
    ONLINE(1, "已上线")
    ;

    @Setter @Getter
    private Integer state;

    @Setter @Getter
    private String desc;

    public static NetworkServerStateEnum fromValue(Integer value) {
        return Arrays.stream(values()).filter(state -> {
            if (state.getState().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
