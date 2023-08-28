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

package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class RegistryBoxEntityRepository implements PanacheRepository<RegistryBoxEntity> {
    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";

    // 根据box_uuid、box_reg_key查询资源
    private static final String FIND_BY_BOXUUID_BOXREGKEY = "box_uuid=?1 AND box_reg_key=?2";

    // 根据network_client_id、network_secret_key查询资源
    private static final String FIND_BY_CLIENTID_SECRETKEY = "network_client_id=?1 AND network_secret_key=?2";

    // 根据box_uuids查询资源
    private static final String FIND_BY_BOXUUIDS = "box_uuid in (?1)";

    // 根据network_client_id查询资源
    private static final String FIND_BY_CLIENTID = "network_client_id=?1";

    public Optional<RegistryBoxEntity> findByBoxUUID(String boxUUID) {
        return this.find(FIND_BY_BOXUUID, boxUUID).firstResultOptional();
    }

    public Optional<RegistryBoxEntity> findByBoxUUIDAndBoxRegKey(String boxUUID, String boxRegKey) {
        return this.find(FIND_BY_BOXUUID_BOXREGKEY, boxUUID, boxRegKey).firstResultOptional();
    }

    public Optional<RegistryBoxEntity> findByClientIdAndSecretKey(String clientId, String secretKey) {
        return this.find(FIND_BY_CLIENTID_SECRETKEY, clientId, secretKey).firstResultOptional();
    }

    public List<RegistryBoxEntity> findByBoxUUIDs(List<String> boxUUIDs) {
        return this.find(FIND_BY_BOXUUIDS, boxUUIDs).list();
    }

    public Optional<RegistryBoxEntity> findByClientId(String networkClientId) {
        return this.find(FIND_BY_CLIENTID, networkClientId).firstResultOptional();
    }

    @Transactional
    public void updateSecretKeyAndSaltByBoxUUID(String hashSecretKey, String salt, String boxUUID) {
        update("set network_secret_key=?1, network_secret_salt=?2, updated_at=now() where box_uuid=?3 ", hashSecretKey, salt, boxUUID);
    }
}
