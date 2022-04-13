package xyz.eulix.platform.services.cache;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Network Server Route 缓存键值对
 */
@Setter
@Getter
public class NSRoute {
    @Schema(description = "用户域名")
    private String userDomain;

    @Schema(description = "network server 地址 & network client id")
    private String networkInfo;

    public NSRoute(String userDomain, String networkInfo) {
        this.userDomain = userDomain;
        this.networkInfo = networkInfo;
    }
}