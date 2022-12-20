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
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class SubdomainEntityRepository implements PanacheRepository<SubdomainEntity> {
    // 根据user_domain查询资源
    private static final String FIND_BY_USER_DOMAIN = "user_domain=?1";

    // 根据subdomain查询资源
    private static final String FIND_BY_SUBDOMAIN = "subdomain=?1";

    // 根据subdomain更新资源
    private static final String UPDATE_BY_SUBDOMAIN = "user_id=?1, state=?2, updated_at=now(), expires_at=null where subdomain=?3";

    // 根据box_uuid、user_id查询资源
    private static final String FIND_BY_BOXUUID_USERID = "box_uuid=?1 AND user_id=?2";

    // 根据box_uuid、user_id、state查询资源
    private static final String FIND_BY_BOXUUID_USERID_STATE = "box_uuid=?1 AND user_id=?2 AND state=?3";

    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";

    // 根据state查询资源
    private static final String FIND_BY_STATE = "state=?1";

    // 根据box_uuid、user_id更新资源
    private static final String UPDATE_BY_BOXUUID_USERID = "subdomain=?1, user_domain=?2, updated_at=now() where box_uuid=?3 AND user_id=?4";

    // 根据box_uuid、user_id、subdomain更新资源状态
    private static final String UPDATE_STATE_BY_BOXUUID_USERID_SUBDOMAIN = "state=?1, updated_at=now() where box_uuid=?2 AND user_id=?3 AND subdomain=?4";

    // 根据box_uuid更新资源状态
    private static final String UPDATE_STATE_BY_BOXUUID = "state=?1, updated_at=now() where box_uuid=?2";

    // 根据box_uuid、user_id更新资源状态
    private static final String UPDATE_STATE_BY_BOXUUID_USERID = "state=?1, updated_at=now() where box_uuid=?2 AND user_id=?3";

    // 根据box_uuid更新资源状态、有效期
    private static final String UPDATE_STATE_EXPIRES_AT_BY_BOXUUID = "state=?1, updated_at=now(), expires_at=?2 where box_uuid=?3";

    public Optional<SubdomainEntity> findByUserDomain(String userDomain) {
        return this.find(FIND_BY_USER_DOMAIN, userDomain).firstResultOptional();
    }

    public Optional<SubdomainEntity> findBySubdomain(String subdomain) {
        return this.find(FIND_BY_SUBDOMAIN, subdomain).firstResultOptional();
    }

    public void updateBySubdomain(String userId, Integer state, String subdomain) {
        this.update(UPDATE_BY_SUBDOMAIN, userId, state, subdomain);
    }

    public void deleteSubdomainByUserId(String boxUUID, String userId) {
        this.delete(FIND_BY_BOXUUID_USERID, boxUUID, userId);
    }

    public void deleteSubdomainByBoxUUID(String boxUUID) {
        this.delete(FIND_BY_BOXUUID, boxUUID);
    }

    public Optional<SubdomainEntity> findByBoxUUIDAndUserIdAndState(String boxUUID, String userId, Integer state){
        return this.find(FIND_BY_BOXUUID_USERID_STATE, boxUUID, userId, state).firstResultOptional();
    }

    // 用正则来匹配subdomain
    public List<SubdomainEntity> findByRegularExpression(String regex){
        // 需要使用 mysql 特有的 regexp/rlike 关键字来进行正则查询.
        return getEntityManager().createNamedQuery("SubdomainEntity.findByRegexp").setParameter("regexp", regex).getResultList();
    }

    public void updateSubdomainByBoxUUIDAndUserId(String boxUUID, String userId, String subdomain, String userDomain) {
        this.update(UPDATE_BY_BOXUUID_USERID, subdomain, userDomain, boxUUID, userId);
    }

    public void updateStateByBoxUUIDAndUserIdAndSubdomain(String boxUUID, String userId, String subdomain, Integer state) {
        this.update(UPDATE_STATE_BY_BOXUUID_USERID_SUBDOMAIN, state, boxUUID, userId, subdomain);
    }

    public void updateStateByBoxUUID(String boxUUID, Integer state) {
        this.update(UPDATE_STATE_BY_BOXUUID, state, boxUUID);
    }

    public void updateStateByBoxUUIDAndUserId(String boxUUID, String userId, Integer state) {
        this.update(UPDATE_STATE_BY_BOXUUID_USERID, state, boxUUID, userId);
    }

    public void updateStateAndExpiresAtByBoxUUID(String boxUUID, Integer state, OffsetDateTime offsetDateTime) {
        this.update(UPDATE_STATE_EXPIRES_AT_BY_BOXUUID, state, offsetDateTime, boxUUID);
    }

    @Transactional
    public void save(SubdomainEntity subdomainEntity) {
        this.persist(subdomainEntity);
    }

    public List<SubdomainEntity> findByBoxUUId(String boxUUID) {
        return this.find(FIND_BY_BOXUUID, boxUUID).list();
    }

    public List<SubdomainEntity> findByBoxUUIdAndUserId(String boxUUID, String userId) {
        return this.find(FIND_BY_BOXUUID_USERID, boxUUID, userId).list();

    }

    public List<SubdomainEntity> findByState(Integer state) {
        return this.find(FIND_BY_STATE, state).list();
    }
}
