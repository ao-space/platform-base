package xyz.eulix.platform.services.auth.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.auth.entity.BoxInfoEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BoxInfoEntityRepository implements PanacheRepository<BoxInfoEntity> {

    public BoxInfoEntity findByPkey(String pkey) {
        return this.find("pkey", pkey).firstResult();
    }
}
