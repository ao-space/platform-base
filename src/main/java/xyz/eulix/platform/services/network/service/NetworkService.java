package xyz.eulix.platform.services.network.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.network.dto.NetworkAuthReq;
import xyz.eulix.platform.services.network.dto.NetworkServerRes;
import xyz.eulix.platform.services.network.entity.NetworkRouteEntity;
import xyz.eulix.platform.services.network.entity.NetworkServerEntity;
import xyz.eulix.platform.services.network.repository.NetworkRouteEntityRepository;
import xyz.eulix.platform.services.network.repository.NetworkServerEntityRepository;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NetworkService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;

    @Inject
    NetworkServerEntityRepository serverEntityRepository;

    @Inject
    NetworkRouteEntityRepository routeEntityRepository;

    /**
     * 计算network路由
     *
     * @param networkClientId networkClientId
     */
    public void calculateNetworkRoute(String networkClientId) {
        // 查询server列表
        List<NetworkServerEntity> serverEntities = serverEntityRepository.findAll().list();
        // hash算法
        NetworkServerEntity serverEntity = allocateAlgotithm(serverEntities, networkClientId);
        Long networkServerId = serverEntity.getId();

        // 插入路由结果
        NetworkRouteEntity routeEntity = new NetworkRouteEntity();
        {
            routeEntity.setClientId(networkClientId);
            routeEntity.setServerId(networkServerId);
        }
        routeEntityRepository.persist(routeEntity);
    }

    private NetworkServerEntity allocateAlgotithm(List<NetworkServerEntity> serverEntities, String networkClientId) {
        int hash = networkClientId.hashCode();
        int index = (hash & Integer.MAX_VALUE) % serverEntities.size();
        return serverEntities.get(index);
    }

    /**
     * 认证 network client 身份
     *
     * @param networkAuthReq networkAuthReq
     * @return 是否通过
     */
    public Boolean networkClientAuth(NetworkAuthReq networkAuthReq) {
        return registryService.networkClientAuth(networkAuthReq.getClientId(), networkAuthReq.getSecretKey());
    }

    public NetworkServerRes networkServerDetail(String networkClientId) {
        // 查询映射关系
        Optional<NetworkRouteEntity> routeEntityOp = routeEntityRepository.findByClientId(networkClientId);
        if (routeEntityOp.isEmpty()) {
            LOG.errorv("network client does not exist, network client id:{0}", networkClientId);
            throw new ServiceOperationException(ServiceError.NETWORK_CLIENT_NOT_EXIST);
        }
        Long networkServerId = routeEntityOp.get().getServerId();
        // 查询network server详情
        NetworkServerEntity serverEntity = serverEntityRepository.findById(networkServerId);
        return networkServerEntityToRes(serverEntity);
    }

    private NetworkServerRes networkServerEntityToRes(NetworkServerEntity serverEntity) {
        return NetworkServerRes.of(serverEntity.getProtocol() + "://" + serverEntity.getAddr() + ":" + serverEntity.getPort());
    }

    @Transactional
    public void deleteByClientID(String clientId) {
        routeEntityRepository.deleteByClientID(clientId);
    }
}
