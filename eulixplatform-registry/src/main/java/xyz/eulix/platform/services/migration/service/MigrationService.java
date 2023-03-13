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

package xyz.eulix.platform.services.migration.service;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.services.cache.NSRClient;
import xyz.eulix.platform.services.cache.NSRRedirectStateEnum;
import xyz.eulix.platform.services.cache.NSRoute;
import xyz.eulix.platform.services.migration.dto.*;
import xyz.eulix.platform.services.network.service.NetworkService;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainStateEnum;
import xyz.eulix.platform.services.registry.entity.RegistryUserEntity;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.token.entity.BoxTokenEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class MigrationService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;
    @Inject
    NSRClient nsrClient;
    @Inject
    SubdomainEntityRepository subdomainEntityRepository;
    @Inject
    NetworkService networkService;

    @Logged
    public BoxMigrationResult migration(BoxMigrationInfo boxMigrationInfo, BoxTokenEntity boxTokenEntity) {
        registryService.resetBox(boxTokenEntity.getBoxUUID());
        LOG.infov("[Migration]: reset box info before migration succeed, boxUUID:{0}", boxTokenEntity.getBoxUUID());

        var boxRegistryResult = registryService.registryBox(boxTokenEntity, boxMigrationInfo.getNetworkClientId());
        LOG.infov("[Migration]: registry box succeed, boxUUID:{0}", boxTokenEntity.getBoxUUID());

        List<UserMigrationInfo> userInfos = new ArrayList<>();
        for (UserMigrationInfo userMigrationInfo : boxMigrationInfo.getUserInfos()) {
            userInfos.add(userMigration(userMigrationInfo, boxTokenEntity.getBoxUUID()));
        }
        return BoxMigrationResult.of(boxTokenEntity.getBoxUUID(), boxRegistryResult.getNetworkClient(), userInfos);
    }

    @Transactional
    @Logged
    public UserMigrationInfo userMigration(UserMigrationInfo userMigrationInfo, String boxUUID) {
        String subdomain = userMigrationInfo.getUserDomain().split("\\.")[0];
        // 注册域名
        SubdomainEntity subdomainEntity;
        Optional<SubdomainEntity> subdomainEntityOp = subdomainEntityRepository.findBySubdomain(subdomain);
        if (subdomainEntityOp.isPresent()) {
            registryService.isSubdomainUsed(subdomainEntityOp.get(), boxUUID, userMigrationInfo.getUserId());
            subdomainEntity = subdomainEntityOp.get();
            LOG.infov("[Migration]: subdomain exists, boxUUID:{0}, userId:{1}, subdomain:{2}", boxUUID, userMigrationInfo.getUserId(), subdomain);
        } else {
            subdomainEntity = registryService.migrationSubdomainGen(boxUUID, subdomain);
            if (!userMigrationInfo.getUserDomain().equals(subdomainEntity.getUserDomain())) {
                userMigrationInfo.setUserDomain(subdomainEntity.getUserDomain());
            }
            LOG.infov("[Migration]: subdomain does not exist, generate it, boxUUID:{0}, userId:{1}, subdomain:{2}", boxUUID,
                    userMigrationInfo.getUserId(), subdomainEntity.getSubdomain());
        }
        // 注册用户
        RegistryUserEntity userEntity = registryService.registryUser(boxUUID, userMigrationInfo.getUserId(),
                RegistryTypeEnum.fromValue(userMigrationInfo.getUserType()));
        // 修改域名状态
        subdomainEntityRepository.updateBySubdomain(userMigrationInfo.getUserId(), SubdomainStateEnum.USED.getState(),
                subdomainEntity.getSubdomain());

        // 添加用户面路由：用户域名 - network server 地址 & network client id
        networkService.cacheNSRoute(subdomainEntity.getUserDomain(), boxUUID);
        LOG.infov("[Migration]: registry user succeed, boxUUID:{0}, userId:{1}", boxUUID, userMigrationInfo.getUserId());

        // 注册客户端
        List<ClientMigrationInfo> clientMigrationInfos = clientMigration(userMigrationInfo.getClientInfos(), boxUUID, userMigrationInfo.getUserId());

        return UserMigrationInfo.of(userEntity.getUserId(), subdomainEntity.getUserDomain(),
                userEntity.getRegistryType(), clientMigrationInfos);
    }

    @Logged
    public List<ClientMigrationInfo> clientMigration(List<ClientMigrationInfo> clientMigrationInfos, String boxUUID, String userId) {
        List<ClientMigrationInfo> clientMigrationResults = new ArrayList<>();
        for (ClientMigrationInfo clientMigrationInfo : clientMigrationInfos) {
            var registryClientEntity = registryService.registryClient(boxUUID, userId,
                    clientMigrationInfo.getClientUUID(), RegistryTypeEnum.fromValue(clientMigrationInfo.getClientType()));
            clientMigrationResults.add(ClientMigrationInfo.of(registryClientEntity.getClientUUID(), registryClientEntity.getRegistryType()));
            LOG.infov("[Migration]: registry client succeed, boxUUID:{0}, userId:{1}, clientUUID:{2}", boxUUID, userId,
                    clientMigrationInfo.getClientUUID());
        }
        return clientMigrationResults;
    }

    @Logged
    public MigrationRouteResult migrationRoute(MigrationRouteInfo migrationRouteInfo, BoxTokenEntity boxTokenEntity) {
        for (UserDomainRouteInfo route : migrationRouteInfo.getUserDomainRouteInfos()) {
            // 根据boxUUID、userId查询全部"未迁移且非临时"用户域名
            List<SubdomainEntity> subdomains = getNotMigratedUserDomains(route.getUserId(), boxTokenEntity.getBoxUUID());
            for (SubdomainEntity subdomain : subdomains) {
                // 计算缓存value
                NSRoute nsRoute = nsrClient.getNSRoute(subdomain.getUserDomain());
                var networkInfo = getNetworkInfo(nsRoute.getNetworkInfo());
                nsrClient.setRedirect(subdomain.getUserDomain(), networkInfo.getServerAddr(), networkInfo.getClientId(),
                        route.getUserDomainRedirect(), NSRRedirectStateEnum.NORMAL);
                LOG.infov("[Migration]: route userdomain succeed, boxUUID:{0}, userId:{1}, userDomain:{2}, redirect:{3}, state:{4}",
                        boxTokenEntity.getBoxUUID(), route.getUserId(), subdomain.getUserDomain(), route.getUserDomainRedirect(), NSRRedirectStateEnum.NORMAL);
            }
        }
        registryService.resetBoxAfterRoute(boxTokenEntity.getBoxUUID());
        LOG.infov("[Migration]: reset box info after route succeed, boxUUID:{0}", boxTokenEntity.getBoxUUID());
        return MigrationRouteResult.of(boxTokenEntity.getBoxUUID(), migrationRouteInfo.getUserDomainRouteInfos());
    }

    public List<SubdomainEntity> getNotMigratedUserDomains(String userId, String boxUUID) {
        List<SubdomainEntity> subdomainEntities = subdomainEntityRepository.findByBoxUUIdAndUserId(boxUUID, userId);
        return subdomainEntities.stream().filter(subdomainEntity -> {
            if (SubdomainStateEnum.MIGRATED.getState().equals(subdomainEntity.getState()) ||
                    SubdomainStateEnum.TEMPORARY.getState().equals(subdomainEntity.getState())) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public NetworkInfo getNetworkInfo(String networkInfo) {
        String[] info = networkInfo.split(",");
        if (info.length >= 3) {
            return NetworkInfo.of(info[0], info[1], info[2]);
        }
        return NetworkInfo.of(info[0], info[1], null);
    }

    @Scheduled(cron = "{app.schedule.migration.cron-expr}")
    void cronJob(ScheduledExecution execution) {
        LOG.infov("[Migration]: execute schedule task: update subdomain state.");
        List<SubdomainEntity> subdomainEntities = subdomainEntityRepository.findByState(SubdomainStateEnum.MIGRATED.getState());
        subdomainEntities.forEach(subdomainEntity -> {
            try {
                if (subdomainEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
                    // 计算缓存value
                    NSRoute nsRoute = nsrClient.getNSRoute(subdomainEntity.getUserDomain());
                    if (CommonUtils.isNullOrEmpty(nsRoute.getNetworkInfo())) {
                        subdomainEntityRepository.updateStateByBoxUUIDAndUserIdAndSubdomain(subdomainEntity.getBoxUUID(), subdomainEntity.getUserId(),
                                subdomainEntity.getSubdomain(), SubdomainStateEnum.TEMPORARY.getState());
                        LOG.warnv("[Migration]: userdomain route does not exist, boxUUID:{0}, userId:{1}, userDomain:{2}",
                                subdomainEntity.getBoxUUID(), subdomainEntity.getUserId(), subdomainEntity.getUserDomain());
                        return;
                    }
                    NetworkInfo networkInfo = getNetworkInfo(nsRoute.getNetworkInfo());
                    nsrClient.setRedirect(subdomainEntity.getUserDomain(), networkInfo.getServerAddr(), networkInfo.getClientId(),
                            networkInfo.getUserDomain(), NSRRedirectStateEnum.EXPIRED);
                    subdomainEntityRepository.updateStateByBoxUUIDAndUserIdAndSubdomain(subdomainEntity.getBoxUUID(), subdomainEntity.getUserId(),
                            subdomainEntity.getSubdomain(), SubdomainStateEnum.TEMPORARY.getState());
                    LOG.infov("[Migration]: update userdomain route state succeed, boxUUID:{0}, userId:{1}, userDomain:{2}, redirect:{3}, state:{4}",
                            subdomainEntity.getBoxUUID(), subdomainEntity.getUserId(), subdomainEntity.getUserDomain(), networkInfo.getUserDomain(), NSRRedirectStateEnum.EXPIRED);
                }
            } catch (Exception e) {
                LOG.warnv(e, "[Migration]: update userdomain route state error, boxUUID:{0}, userId:{1}, userDomain:{2}",
                        subdomainEntity.getBoxUUID(), subdomainEntity.getUserId(), subdomainEntity.getUserDomain());
            }
        });
    }
}
