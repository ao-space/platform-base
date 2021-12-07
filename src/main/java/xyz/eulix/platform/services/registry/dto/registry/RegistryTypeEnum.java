package xyz.eulix.platform.services.registry.dto.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum RegistryTypeEnum {
    CLIENT("client", "客户端"),
    BOX("box", "傲来盒子"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static RegistryTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> {
            if (appType.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
