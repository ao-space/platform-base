package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;

import javax.enterprise.context.ApplicationScoped;
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

    public Optional<RegistryBoxEntity> findByBoxUUID(String boxUUID) {
        return this.find(FIND_BY_BOXUUID, boxUUID).firstResultOptional();
    }

    public Optional<RegistryBoxEntity> findByBoxUUIDAndBoxRegKey(String boxUUID, String boxRegKey) {
        return this.find(FIND_BY_BOXUUID_BOXREGKEY, boxUUID, boxRegKey).firstResultOptional();
    }

    public Optional<RegistryBoxEntity> findByClientIdAndSecretKey(String clientId, String secretKey) {
        return this.find(FIND_BY_CLIENTID_SECRETKEY, clientId, secretKey).firstResultOptional();
    }
}
