package xyz.eulix.platform.services.network.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.network.entity.NetworkRouteEntity;
import xyz.eulix.platform.services.network.entity.NetworkServerEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class NetworkRouteEntityRepository implements PanacheRepository<NetworkRouteEntity> {
    // 根据network_client_id查询资源
    private static final String FIND_BY_CLIENTID = "network_client_id=?1";

    public Optional<NetworkRouteEntity> findByClientId(String networkClientId) {
        return this.find(FIND_BY_CLIENTID, networkClientId).firstResultOptional();
    }

    public void deleteByClientID(String clientId) {
        this.delete(FIND_BY_CLIENTID, clientId);
    }
}
