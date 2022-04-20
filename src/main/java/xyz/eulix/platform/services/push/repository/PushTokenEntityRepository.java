package xyz.eulix.platform.services.push.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.push.entity.PushTokenEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PushTokenEntityRepository implements PanacheRepository<PushTokenEntity> {
    // 根据client_uuid查询资源
    private static final String FIND_BY_CLIENTUUID = "client_uuid=?1";

    // 根据client_uuids查询资源
    private static final String FIND_BY_CLIENTUUIDS = "client_uuid in (?1)";

    // 根据id更新资源
    private static final String UPDATE_BY_ID = "deviceToken=?1, extra=?2, updated_at=now() where client_uuid=?3";

    public Optional<PushTokenEntity> findByClientUUID(String clientUUID) {
        return this.find(FIND_BY_CLIENTUUID, clientUUID).singleResultOptional();
    }

    public void updateByClientUUID(String clientUUID, String deviceToken, String extra) {
        this.update(UPDATE_BY_ID, deviceToken, extra, clientUUID);
    }

    public List<PushTokenEntity> findByClientUUIDs(List<String> clientUUIDs) {
        return this.find(FIND_BY_CLIENTUUID, clientUUIDs).list();
    }
}
