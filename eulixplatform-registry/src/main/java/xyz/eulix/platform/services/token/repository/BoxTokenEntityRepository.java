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

package xyz.eulix.platform.services.token.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

import xyz.eulix.platform.services.token.entity.BoxTokenEntity;

@ApplicationScoped

public class BoxTokenEntityRepository implements PanacheRepository<BoxTokenEntity> {
    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";
    // 根据box_reg_key查询资源
    private static final String FIND_BY_BOX_REG_KEY = "box_reg_key=?1";
    // 根据box_uuid、box_reg_key查询资源
    private static final String FIND_BY_BOXUUID_BOX_REG_KEY = "box_uuid=?1 AND box_reg_key=?2";

    public Optional<BoxTokenEntity> findByBoxUUID(String boxUUID) {
        return this.find(FIND_BY_BOXUUID, boxUUID).firstResultOptional();
    }

    public Optional<BoxTokenEntity> findByBoxRegKey(String boxRegKey) {
        return this.find(FIND_BY_BOX_REG_KEY, boxRegKey).firstResultOptional();
    }

    public Optional<BoxTokenEntity> findByBoxUUIDAndRegKey(String boxUUID, String boxRegKey) {
        return this.find(FIND_BY_BOXUUID_BOX_REG_KEY, boxUUID, boxRegKey).firstResultOptional();
    }
}
