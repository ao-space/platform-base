package xyz.eulix.platform.services.registry.dto.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum SubdomainStateEnum {
    TEMPORARY(0, "临时"),
    USED(1, "已使用")
    ;

    @Setter @Getter
    private Integer state;

    @Setter @Getter
    private String desc;

    public static SubdomainStateEnum fromValue(Integer value) {
        return Arrays.stream(values()).filter(state -> {
            if (state.getState().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
