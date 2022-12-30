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

package xyz.eulix.platform.services.registry.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainStateEnum;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class SubdomainService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ApplicationProperties properties;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    @Transactional
    public void updateSubdomain(String boxUUID, String userId, String subdomain, String userDomain, String subdomainOld) {
        // subdomain
        subdomainEntityRepository.updateStateByBoxUUIDAndUserIdAndSubdomain(boxUUID, userId, subdomainOld, SubdomainStateEnum.HISTORY.getState());
        SubdomainEntity subdomainEntity = new SubdomainEntity();
        {
            subdomainEntity.setBoxUUID(boxUUID);
            subdomainEntity.setUserId(userId);
            subdomainEntity.setSubdomain(subdomain);
            subdomainEntity.setUserDomain(userDomain);
            subdomainEntity.setState(SubdomainStateEnum.USED.getState());
        }
        subdomainEntityRepository.persist(subdomainEntity);
    }

    @Transactional
    public void updateSubdomainState(String boxUUID, String userId, String subdomain, String subdomainOld) {
        // 更新旧域名状态为HISTORY
        subdomainEntityRepository.updateStateByBoxUUIDAndUserIdAndSubdomain(boxUUID, userId, subdomainOld, SubdomainStateEnum.HISTORY.getState());
        // 更新新域名状态为USED
        subdomainEntityRepository.updateStateByBoxUUIDAndUserIdAndSubdomain(boxUUID, userId, subdomain, SubdomainStateEnum.USED.getState());
    }
}
