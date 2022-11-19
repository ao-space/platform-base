/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.services.network.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.cache.NSRClient;
import xyz.eulix.platform.services.network.dto.NetworkAuthReq;
import xyz.eulix.platform.services.network.dto.NetworkServerExtraInfo;
import xyz.eulix.platform.services.network.dto.NetworkServerRes;
import xyz.eulix.platform.services.network.dto.StunServerRes;
import xyz.eulix.platform.services.network.entity.NetworkRouteEntity;
import xyz.eulix.platform.services.network.entity.NetworkServerEntity;
import xyz.eulix.platform.services.network.repository.NetworkRouteEntityRepository;
import xyz.eulix.platform.services.network.repository.NetworkServerEntityRepository;
import xyz.eulix.platform.services.provider.ProviderFactory;
import xyz.eulix.platform.services.provider.inf.RegistryProvider;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.RegistryBoxEntityRepository;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;

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

    @Inject
    RegistryBoxEntityRepository registryBoxEntityRepository;

    @Inject
    NSRClient nsrClient;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    @Inject
    OperationUtils operationUtils;

    @Inject
    ProviderFactory providerFactory;

    /**
     * 计算network路由
     *
     * @param networkClientId networkClientId
     */
    public void calculateNetworkRoute(String networkClientId) {
        RegistryProvider registryProvider = providerFactory.getRegistryProvider();
        Long networkServerId = registryProvider.calculateNetworkRoute(networkClientId);

        // 插入路由结果
        NetworkRouteEntity routeEntity = new NetworkRouteEntity();
        {
            routeEntity.setClientId(networkClientId);
            routeEntity.setServerId(networkServerId);
        }
        routeEntityRepository.persist(routeEntity);
    }

    /**
     * 添加用户面路由：用户域名 - network server 地址 & network client id
     *
     * @param userDomain
     * @param boxUUID
     */
    public void cacheNSRoute(String userDomain, String boxUUID) {
        // 查询 networkClientId（box 注册信息）
        Optional<RegistryBoxEntity> registryBoxEntityOp = registryBoxEntityRepository.findByBoxUUID(boxUUID);
        if (registryBoxEntityOp.isEmpty()) {
            LOG.warnv("box uuid had not registered, boxuuid:{0}", boxUUID);
            throw new ServiceOperationException(ServiceError.BOX_NOT_REGISTERED);
        }
        String networkClientId = registryBoxEntityOp.get().getNetworkClientId();
        // 查询 network server 地址
        NetworkServerEntity serverEntity = getNetworkServer(networkClientId);
        String networkServerLocalAddr = serverEntity.getIdentifier();
        // 缓存用户面路由
        nsrClient.setNSRoute(userDomain, networkServerLocalAddr, networkClientId);
    }

    public void expireNSRoute(String userDomain, Integer expireSeconds) {
        nsrClient.expireNSRoute(userDomain, expireSeconds.toString());
    }

    /**
     * 认证 network client 身份
     *
     * @param networkAuthReq networkAuthReq
     * @return 是否通过
     */
    public Boolean networkClientAuth(NetworkAuthReq networkAuthReq) {
        return networkClientAuth(networkAuthReq.getClientId(), networkAuthReq.getSecretKey());
    }

    public Boolean networkClientAuth(String clientId, String secretKey) {
        return registryService.networkClientAuth(clientId, secretKey);
    }

    /**
     * 查询最新 network server 信息
     *
     * @param networkClientId network client id
     * @return network server 信息
     */
    public NetworkServerRes networkServerDetail(String networkClientId) {
        NetworkServerEntity serverEntity = getNetworkServer(networkClientId);
        return networkServerEntityToRes(serverEntity);
    }

    private NetworkServerEntity getNetworkServer(String networkClientId) {
        // 查询映射关系
        Optional<NetworkRouteEntity> routeEntityOp = routeEntityRepository.findByClientId(networkClientId);
        if (routeEntityOp.isEmpty()) {
            LOG.errorv("network client does not exist, network client id:{0}", networkClientId);
            throw new ServiceOperationException(ServiceError.NETWORK_CLIENT_NOT_EXIST);
        }
        Long networkServerId = routeEntityOp.get().getServerId();
        // 查询network server详情
        Optional<NetworkServerEntity> serverEntityOp = serverEntityRepository.findByIdOptional(networkServerId);
        if (serverEntityOp.isEmpty()) {
            LOG.errorv("network server does not exist, network server id:{0}", networkServerId);
            throw new ServiceOperationException(ServiceError.NETWORK_SERVER_NOT_EXIST);
        }
        return serverEntityOp.get();
    }

    private NetworkServerRes networkServerEntityToRes(NetworkServerEntity serverEntity) {
        return NetworkServerRes.of(getNetworkServerAddr(serverEntity.getProtocol(), serverEntity.getAddr(), serverEntity.getPort()),
                getExtraInfo(serverEntity).getStunAddress());
    }

    private String getNetworkServerAddr(String protocal, String addr, Integer port) {
        return protocal + "://" + addr + ":" + port;
    }

    private String getNetworkServerAddr(String addr, Integer port) {
        return addr + ":" + port;
    }

    @Transactional
    public void deleteByClientID(String clientId) {
        routeEntityRepository.deleteByClientID(clientId);
    }

    public StunServerRes stunServerDetail(String subdomain) {
        // 查询boxUUID
        Optional<SubdomainEntity> subdomainEntityOp = subdomainEntityRepository.findBySubdomain(subdomain);
        if (subdomainEntityOp.isEmpty()) {
            LOG.warnv("subdomain does not exist, subdomain:{0}", subdomain);
            throw new ServiceOperationException(ServiceError.SUBDOMAIN_NOT_EXIST);
        }
        // 查询盒子的network client信息
        Optional<RegistryBoxEntity> registryBoxEntityOp = registryBoxEntityRepository.findByBoxUUID(subdomainEntityOp.get().getBoxUUID());
        String networkClientId = registryBoxEntityOp.get().getNetworkClientId();
        // 查询映射关系
        Optional<NetworkRouteEntity> routeEntityOp = routeEntityRepository.findByClientId(networkClientId);
        Long networkServerId = routeEntityOp.get().getServerId();
        // 查询network server详情
        NetworkServerEntity serverEntity = serverEntityRepository.findById(networkServerId);
        return StunServerRes.of(getExtraInfo(serverEntity).getStunAddress());
    }

    private NetworkServerExtraInfo getExtraInfo(NetworkServerEntity serverEntity) {
        if (CommonUtils.isNullOrEmpty(serverEntity.getExtra())) {
            LOG.errorv("network server extra info is illegal");
            return NetworkServerExtraInfo.of();
        }
        return operationUtils.jsonToObject(serverEntity.getExtra(), NetworkServerExtraInfo.class);
    }
}
