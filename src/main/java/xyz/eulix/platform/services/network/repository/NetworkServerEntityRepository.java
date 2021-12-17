package xyz.eulix.platform.services.network.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.network.entity.NetworkServerEntity;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class NetworkServerEntityRepository implements PanacheRepository<NetworkServerEntity> {
}
