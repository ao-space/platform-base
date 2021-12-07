package xyz.eulix.platform.services.network.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.network.entity.NetworkRouteEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NetworkRouteEntityRepository implements PanacheRepository<NetworkRouteEntity> {
}
