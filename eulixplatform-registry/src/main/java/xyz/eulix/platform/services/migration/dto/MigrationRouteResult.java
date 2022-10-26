package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * 用户域名映射信息
 */
@Data(staticConstructor = "of")
public class MigrationRouteResult {
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @Schema(description = "subdomain映射关系")
    private List<SubdomainRouteInfo> subdomainRoutes;
}
