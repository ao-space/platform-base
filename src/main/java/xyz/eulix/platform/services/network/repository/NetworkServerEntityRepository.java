package xyz.eulix.platform.services.network.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.network.entity.NetworkServerEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NetworkServerEntityRepository implements PanacheRepository<NetworkServerEntity> {

}
