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
