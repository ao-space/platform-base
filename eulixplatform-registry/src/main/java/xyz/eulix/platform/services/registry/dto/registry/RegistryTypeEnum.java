package xyz.eulix.platform.services.registry.dto.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum RegistryTypeEnum {
    BOX("box", "傲来盒子"),
    USER_ADMIN("user_admin", "用户管理员"),
    USER_MEMBER("user_member", "用户成员"),
    CLIENT_BIND("client_bind", "绑定类型客户端"),
    CLIENT_AUTH("client_auth", "授权类型客户端"),
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
