package xyz.eulix.platform.services.basic.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import java.util.List;

/**
 * 空间平台API
 */
@Data(staticConstructor = "of")
public class PlatformApi {
    @Schema(description = "http method")
    private final String method;

    @Schema(description = "uri，如/platform/v*/api/registry/box")
    private final String uri;

    @Schema(description = "简略uri，以四级目录开头，如/registry/box")
    private final String briefUri;

    @Schema(description = "兼容的API版本列表，如1，2，3")
    private final List<Integer> compatibleVersions;

    @Schema(description = "API分类（空间平台基础api、空间平台扩展api、产品服务api），取值：basic_api、extension_api、product_service_api")
    @ValueOfEnum(enumClass = PlatformApiTypeEnum.class, valueMethod = "getName")
    private final String type;

    @Schema(description = "API描述")
    private final String desc;
}
