package xyz.eulix.platform.services.push.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.push.entity.PushTokenEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PushTokenEntityRepository implements PanacheRepository<PushTokenEntity> {
}
