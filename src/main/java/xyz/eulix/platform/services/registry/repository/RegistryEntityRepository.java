package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class RegistryEntityRepository implements PanacheRepository<RegistryEntity> {
    // 根据box_uuid、box_reg_key查询资源
    private static final String FIND_BY_BOXUUID_BOXREGKEY = "box_uuid=?1 AND box_reg_key=?2";

    // 根据box_uuid、client_uuid、box_reg_key查询资源
    private static final String FIND_BY_CLIENTUUID_CLIENTREGKEY = "box_uuid=?1 AND client_uuid=?2 AND client_reg_key=?3";

    // 根据box_uuid、client_uuid查询资源
    private static final String FIND_BY_CLIENTUUID = "box_uuid=?1 AND client_uuid=?2";

    // 根据box_uuid、type查询资源
    private static final String FIND_BY_BOXUUID_TYPE = "box_uuid=?1 AND type=?2";

    // 根据box_uuid、type查询资源
    private static final String FIND_BY_USERDOMAIN = "subdomain=?1";

    public List<RegistryEntity> findAllByBoxUUIDAndBoxRegKey(String boxUUID, String boxRegKey) {
        return this.find(FIND_BY_BOXUUID_BOXREGKEY, boxUUID, boxRegKey).list();
    }

    public List<RegistryEntity> findAllByClientUUIDAndClientRegKey(String boxUUID, String clientUUID, String clientRegKey) {
        return this.find(FIND_BY_CLIENTUUID_CLIENTREGKEY, boxUUID, clientUUID, clientRegKey).list();
    }

    public void deleteByClientUUID(String boxUUID, String clientUUID) {
        this.delete(FIND_BY_CLIENTUUID, boxUUID, clientUUID);
    }

    public PanacheQuery<RegistryEntity> findByBoxUUIDAndType(String boxUUID, String type) {
        return this.find(FIND_BY_BOXUUID_TYPE, boxUUID, type);
    }

    public Optional<RegistryEntity> findByUserDomain(String userDomain) {
        return this.find(FIND_BY_USERDOMAIN, userDomain).singleResultOptional();
    }
}
