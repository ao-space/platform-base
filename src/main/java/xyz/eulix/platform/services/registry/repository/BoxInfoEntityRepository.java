package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.BoxInfoEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class BoxInfoEntityRepository implements PanacheRepository<BoxInfoEntity> {
    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";

    // 根据box_uuids查询资源
    private static final String FIND_BY_BOXUUIDS = "box_uuid in (?1)";

    public Optional<BoxInfoEntity> findByBoxUUID(String boxUUID) {
        return this.find(FIND_BY_BOXUUID, boxUUID).firstResultOptional();
    }

    public void deleteByBoxUUIDS(List<String> boxUUIDs) {
        this.delete(FIND_BY_BOXUUIDS, boxUUIDs);
    }
}
