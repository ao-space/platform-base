package xyz.eulix.platform.services.registry.dto.registry.v2;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum AuthTypeEnum {
    BOX_UUID("box_uuid", "box_uuid 验证"),
    BOX_PUB_KEY("box_pub_key", "box 公钥验证"),

    ;

    @Getter
    private final String name;

    @Getter
    private final String desc;

    public static AuthTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> appType.getName().equals(value)).findFirst().orElseThrow();
    }
}
