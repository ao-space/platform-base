package xyz.eulix.platform.services.provider.inf;

/**
 * 注册相关Provider，可自定义实现
 */
public interface RegistryProvider {
    /**
     * 盒子身份认证
     *
     * @param boxUUID boxUUID
     * @return 是否合法
     */
    Boolean isBoxIllegal(String boxUUID);

    /**
     * 计算network路由
     *
     * @param networkClientId networkClientId
     * @return networkServerId
     */
    Long calculateNetworkRoute(String networkClientId);

    /**
     * 验证子域名是否合法
     *
     * @param subdomain 子域名
     * @return 是否合法
     */
    Boolean isSubdomainIllegal(String subdomain);
}
