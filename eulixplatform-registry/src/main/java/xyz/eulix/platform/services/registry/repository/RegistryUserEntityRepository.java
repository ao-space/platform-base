package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.RegistryUserEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class RegistryUserEntityRepository implements PanacheRepository<RegistryUserEntity> {
    // 根据box_uuid、user_id查询资源
    private static final String FIND_BY_BOXUUID_USERID = "box_uuid=?1 AND user_id=?2";;

    // 根据box_uuid、user_id、user_reg_key查询资源
    private static final String FIND_BY_BOXUUID_USERID_USERREGKEY = "box_uuid=?1 AND user_id=?2 AND user_reg_key=?3";;

    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";

    // 根据type查询资源
    private static final String FIND_BY_TYPE = "type=?1";

    public List<RegistryUserEntity> findAllByUserId(String boxUUID, String userId) {
        return this.find(FIND_BY_BOXUUID_USERID, boxUUID, userId).list();
    }

    public List<RegistryUserEntity> findAllByUserIDAndUserRegKey(String boxUUID, String userId, String userRegKey) {
        return this.find(FIND_BY_BOXUUID_USERID_USERREGKEY, boxUUID, userId, userRegKey).list();
    }

    public void deleteByUserId(String boxUUID, String userId) {
        this.delete(FIND_BY_BOXUUID_USERID, boxUUID, userId);
    }

    public void deleteByBoxUUID(String boxUUID) {
        this.delete(FIND_BY_BOXUUID, boxUUID);
    }

    public List<RegistryUserEntity> findByBoxUUId(String uuid){
        return this.find(FIND_BY_BOXUUID, uuid).list();
    }

    public PanacheQuery<RegistryUserEntity> findAllByType(String type, Integer pageIndex, Integer pageSize) {
        return this.find(FIND_BY_TYPE, type).page(pageIndex, pageSize);
    }
}
