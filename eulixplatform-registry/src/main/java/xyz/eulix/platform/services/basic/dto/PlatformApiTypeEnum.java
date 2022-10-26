package xyz.eulix.platform.services.basic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum PlatformApiTypeEnum {
    BASIC_API("basic_api", "空间平台基础api"),
    EXTENSION_API("extension_api", "空间平台扩展api"),
    PRODUCT_SERVICE_API("product_service_api", "产品服务api"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static PlatformApiTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(apiType -> {
            if (apiType.getName().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
