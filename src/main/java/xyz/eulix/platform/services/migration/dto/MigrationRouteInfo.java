package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户域名映射信息
 */
@Data
public class MigrationRouteInfo {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @NotBlank
    @Schema(description = "盒子的注册码；传参为空时将由平台生成")
    private String boxRegKey;

    @Valid
    @NotEmpty
    @Schema(description = "subdomain映射关系")
    private List<SubdomainRouteInfo> subdomainRoutes;
}
